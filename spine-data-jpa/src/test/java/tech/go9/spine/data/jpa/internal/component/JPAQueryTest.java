/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.go9.spine.data.jpa.internal.component;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import tech.go9.spine.sample.api.domain.City;
import tech.go9.spine.sample.api.domain.Hotel;
import tech.go9.spine.sample.api.domain.QCity;
import tech.go9.spine.sample.api.domain.QHotel;
import tech.go9.spine.sample.api.domain.QReview;
import tech.go9.spine.sample.api.domain.Review;
import tech.go9.spine.sample.api.repository.HotelRepository;
import tech.go9.spine.sample.internal.configuration.SpineDataJpaTestConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpineDataJpaTestConfiguration.class)
@DataJpaTest
@ActiveProfiles("default")
class JPAQueryTest {

	@Autowired
	private TestEntityManager entityManager;

	private QCity city = QCity.city;

	private QHotel hotel = QHotel.hotel;

	private QReview review = QReview.review;

	@Autowired
	private HotelRepository hotelRepository;

	@Test
	void test00() {
		JPAQuery<City> query = new JPAQuery<>(this.entityManager.getEntityManager());
		query.select(city).leftJoin(hotel).where(hotel.name.like("The%"));
		List<City> result = query.fetch();
		Assertions.assertTrue(result.size() > 1);
	}

	@Test
	void test01() {
		JPAQuery<City> query = new JPAQuery<>(this.entityManager.getEntityManager());
		query.select(city).innerJoin(hotel).where(hotel.name.like("The%"));
		List<City> result = query.fetch();
		Assertions.assertTrue(result.size() > 1);
	}

	@Test
	void test02() {
		JPAQuery<Hotel> query = new JPAQuery<>(this.entityManager.getEntityManager());
		query.select(hotel).innerJoin(review).where(review.title.like("Pretty%"));
		List<Hotel> result = query.fetch();
		Assertions.assertTrue(result.size() == 1);
	}

	@Test
	void test03() {
		JPAQuery<Hotel> query = new JPAQuery<>(this.entityManager.getEntityManager());
		query.select(city).leftJoin(hotel).where(hotel.name.like("The%"));
		List<Hotel> result = query.fetch();
		Assertions.assertTrue(result.size() > 1);
	}

	@Test
	void test04() {
		JPAQueryFactory queryFactory = new JPAQueryFactory(this.entityManager.getEntityManager());
		List<City> result = queryFactory.selectFrom(city).leftJoin(city.hotels, hotel).fetchJoin()
				.leftJoin(hotel.reviews, review).fetchJoin().where(review.title.like("Pretty%")).fetch();
		Assertions.assertTrue(result.size() == 1);
		for (City city : result) {
			System.out.println(city.getName());
			for (Hotel hotel : city.getHotels()) {
				System.out.println(hotel.getName());
				for (Review review : hotel.getReviews()) {
					System.out.println(review.getTitle());
					Assertions.assertTrue(review.getTitle().startsWith("Pretty"));
				}
			}
		}
	}

	// @Test
	void paginationTest() {
		int size = 5;
		int page = 0;
		Page<Hotel> hotels;
		do {
			hotels = hotelRepository.findAll(PageRequest.of(page, size), Optional.empty(), Optional.of("reviews"));
			if (hotels.hasNext()) {
				Assertions.assertTrue(hotels.getContent().size() == hotels.getSize());
			}
			page++;
			Assertions.assertEquals(6, hotels.getTotalPages());
			Assertions.assertEquals(27, hotels.getTotalElements());
		}
		while (!hotels.isLast());

		Page<Hotel> hotels1 = hotelRepository.findAll(PageRequest.of(page, size), Optional.empty(), Optional.empty());
		Assertions.assertEquals(hotels1.getTotalElements(), hotels.getTotalElements());

	}

}
