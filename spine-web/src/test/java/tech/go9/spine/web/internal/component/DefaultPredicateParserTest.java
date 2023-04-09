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
package tech.go9.spine.web.internal.component;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import com.querydsl.core.types.Predicate;

import tech.go9.spine.web.internal.usecase.DefaultParsePredicate;
import tech.go9.spine.sample.api.domain.City;
import tech.go9.spine.sample.api.domain.Hotel;
import tech.go9.spine.sample.api.domain.QCity;
import tech.go9.spine.sample.api.domain.Review;
import tech.go9.spine.sample.api.repository.CityRepository;
import tech.go9.spine.sample.api.repository.HotelRepository;
import tech.go9.spine.sample.api.repository.ReviewRepository;
import tech.go9.spine.web.internal.configuration.SpineWebTestConfiguration;

@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(classes = { SpineWebTestConfiguration.class })
class DefaultPredicateParserTest {

	@Autowired
	private DefaultParsePredicate predicateParser;

	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private HotelRepository hotelRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	@Test
	void testGetEntityFilter00() {
		String filterExpression = "name = 'Melbourne'";
		Predicate predicate = predicateParser.execute(City.class, filterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(2, page.getContent().size());
	}

	@Test
	void testGetEntityFilter01() {
		String filterExpression = "name = 'Melbourne' and state = 'Victoria'";
		Predicate predicate = predicateParser.execute(City.class, filterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(1, page.getContent().size());
	}

	@Test
	void testGetEntityFilter02() {
		String filterExpression = "name = 'Melbourne' or state = 'Queensland'";
		Predicate predicate = predicateParser.execute(City.class, filterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(3, page.getContent().size());
	}

	@Test
	void testGetEntityFilter03() {
		String filterExpression = "(name = 'Melbourne' or state = 'Queensland') and id = 2";
		Predicate predicate = predicateParser.execute(City.class, filterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(1, page.getContent().size());
	}

	@Test
	@Disabled
	// TODO
	void testGetEntityFilter04() {
		String filterExpression = "checkInDate = toInstant('2005-05-10+03:00')";
		Predicate predicate = predicateParser.execute(Review.class, filterExpression);
		Page<Review> page = reviewRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(1, page.getContent().size());
	}

	@Test
	void testGetEntityFilter05() {
		String filterExpression = "upper(name) = 'MELBOURNE'";
		Predicate predicate = predicateParser.execute(City.class, filterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(2, page.getContent().size());
	}

	@Test
	void testGetEntityFilter06() {
		String filterExpression = "name = 'MELBOURNE'";
		Predicate predicate = predicateParser.execute(City.class, filterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(0, page.getContent().size());
	}

	@Test
	void testGetEntityFilter07() {
		String filterExpression = "lower(name) = 'melbourne'";
		Predicate predicate = predicateParser.execute(City.class, filterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(2, page.getContent().size());
	}

	@Test
	void testGetEntityFilter08() {
		String filterExpression = "name = 'melbourne'";
		Predicate predicate = predicateParser.execute(City.class, filterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(0, page.getContent().size());
	}

	@Test
	void testGetEntityFilter09() {
		String filterExpression = "details = null";
		Predicate predicate = predicateParser.execute(Review.class, filterExpression);
		Page<Review> page = reviewRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(1, page.getContent().size());
	}

	@Test
	void testGetEntityFilter10() {
		String filterExpression = "hotel.city.name = 'Melbourne'";
		Predicate predicate = predicateParser.execute(Review.class, filterExpression);
		Page<Review> page = reviewRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(7, page.getContent().size());
	}

	@Test
	void testGetEntityFilter11() {
		Predicate predicate = QCity.city.hotels.any().reviews.any().details.isNull();
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals("Melbourne", page.getContent().get(0).getName());
	}

	@Test
	void testGetEntityFilter12() {
		String filterExpression = "hotels.reviews.details = null";
		Predicate predicate = predicateParser.execute(City.class, filterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals("Melbourne", page.getContent().get(0).getName());
	}

	@Test
	void testGetEntityFilter13() {
		String filterExpression = "hotels.reviews.title like 'Pretty%'";
		Predicate predicate = predicateParser.execute(City.class, filterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals("Melbourne", page.getContent().get(0).getName());
	}

	@Test
	void testGetEntityFilter14() {
		String filterExpression = "name like 'Melbour%'";
		Predicate predicate = predicateParser.execute(City.class, filterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(2, page.getContent().size());
	}

	@Test
	void testGetEntityFilter15() {
		Predicate predicate = QCity.city.hotels.any().reviews.any().title.startsWith("Pretty");
		Optional<City> city = cityRepository.findById(2L, predicate);
		Assertions.assertNotNull(city);
		Assertions.assertTrue(city.isPresent());
		Assertions.assertEquals("Melbourne", city.get().getName());
	}

	@Test
	void testGetEntityFilter16() {
		String filterExpression = "hotel.city.name in ('Melbourne', 'Brisbane')";
		Predicate predicate = predicateParser.execute(Review.class, filterExpression);
		Page<Review> page = reviewRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(7, page.getContent().size());
	}

	@Test
	void testGetEntityFilter17() {
		String filterExpression = "tripType = FAMILY";
		Predicate predicate = predicateParser.execute(Review.class, filterExpression);
		Page<Review> page = reviewRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(18, page.getContent().size());
	}

	@Test
	void testGetEntityFilter18() {
		String filterExpression = "reviews.tripType = FAMILY";
		Predicate predicate = predicateParser.execute(Hotel.class, filterExpression);
		Page<Hotel> page = hotelRepository.findAll(PageRequest.of(0, 20), predicate);
		Assertions.assertNotNull(page);
		Assertions.assertEquals(10, page.getContent().size());
	}

}
