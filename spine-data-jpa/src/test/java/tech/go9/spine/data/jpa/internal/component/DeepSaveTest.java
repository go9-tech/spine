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

import java.util.LinkedHashSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import tech.go9.spine.sample.api.domain.City;
import tech.go9.spine.sample.api.domain.Hotel;
import tech.go9.spine.sample.api.repository.CityRepository;
import tech.go9.spine.sample.api.repository.HotelRepository;
import tech.go9.spine.sample.internal.configuration.SpineDataJpaTestConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpineDataJpaTestConfiguration.class)
@DataJpaTest
@ActiveProfiles("default")
class DeepSaveTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private HotelRepository hotelRepository;

	// Preservo the hotels list with orphanRemoval=true
	@Test
	void test01() {
		City city = new City();
		city.setId(2L);
		city.setCountry("Australia");
		city.setName("Melbourne");
		city.setState("Victoria");
		city.setMap("0, 0");
		city = this.cityRepository.saveOne(city);
		this.entityManager.flush();
		Assertions.assertNotNull(city);
		Assertions.assertEquals("0, 0", city.getMap());
		Assertions.assertEquals(1, city.getHotels().size());
	}

	@Test
	void test02() {
		City city = new City();
		city.setId(2L);
		city.setCountry("Australia");
		city.setName("Melbourne");
		city.setState("Victoria");
		city.setMap("0, 0");
		city.setHotels(new LinkedHashSet<Hotel>());
		city = this.cityRepository.saveOne(city);
		this.entityManager.flush();
		Assertions.assertNotNull(city);
		Assertions.assertEquals(0, city.getHotels().size());
	}

	@Test
	void test03() {

		City city = new City();
		city.setId(9L);
		city.setCountry("UK");
		city.setName("Bath");
		city.setState("Somerset");
		city.setMap("0, 0");
		city.setHotels(new LinkedHashSet<Hotel>());

		Hotel hotel = new Hotel();
		hotel.setId(9L);
		hotel.setName("New Bath Priory Hotel");
		hotel.setAddress("Weston Road");
		hotel.setZip("BA1 2XT");
		city.getHotels().add(hotel);

		city = this.cityRepository.saveOne(city);
		this.entityManager.flush();
		Assertions.assertNotNull(city);
		Assertions.assertEquals(1, city.getHotels().size());
	}

	@Test
	void test04() {

		City city = new City();
		city.setId(9L);
		city.setCountry("UK");
		city.setName("Bath");
		city.setState("Somerset");
		city.setMap("0, 0");
		city.setHotels(new LinkedHashSet<Hotel>());

		Hotel hotel = new Hotel();
		hotel.setId(9L);
		hotel.setName("The Bath Priory Hotel");
		hotel.setAddress("Weston Road");
		hotel.setZip("BA1 2XT");
		city.getHotels().add(hotel);

		hotel = new Hotel();
		hotel.setCity(city);
		hotel.setName("Assis Hotel");
		hotel.setAddress("Sapopemba Road");
		hotel.setZip("03227-070");
		city.getHotels().add(hotel);

		city = this.cityRepository.saveOne(city);
		this.entityManager.flush();
		Assertions.assertNotNull(city);
		Assertions.assertEquals(2, city.getHotels().size());
	}

	@Test
	void test05() {

		City city = new City();
		city.setId(9L);
		city.setCountry("UK");
		city.setName("Bath");
		city.setState("Somerset");
		city.setMap("0, 0");

		Hotel hotel = new Hotel();
		hotel.setCity(city);
		hotel.setName("Assis Hotel 2");
		hotel.setAddress("Sapopemba Road");
		hotel.setZip("03227-070");

		hotel = this.hotelRepository.saveOne(hotel);
		this.entityManager.flush();
		Assertions.assertNotNull(hotel);
		Assertions.assertNotNull(hotel.getId());
		Assertions.assertEquals("Assis Hotel 2", hotel.getName());
	}

	@Test
	void test06() {

		City city = new City();
		city.setId(9L);
		city.setCountry("UK");
		city.setName("Bath");
		city.setState("Somerset");
		city.setMap("0, 0");
		city.setHotels(new LinkedHashSet<Hotel>());

		Hotel hotel = new Hotel();
		hotel.setId(9L);
		hotel.setName("The Bath Priory Hotel");
		hotel.setAddress("Weston Road");
		hotel.setZip("BA1 2XT");
		city.getHotels().add(hotel);

		hotel = new Hotel();
		hotel.setCity(city);
		hotel.setName("Assis Hotel");
		hotel.setAddress("Sapopemba Road");
		hotel.setZip("03227-070");
		city.getHotels().add(hotel);

		city = this.cityRepository.saveOne(city);
		this.entityManager.flush();
		Assertions.assertNotNull(city);
		Assertions.assertEquals(2, city.getHotels().size());
	}

	@Test
	void test07() {

		City city = new City();
		city.setCountry("UK");
		city.setName("Assis");
		city.setState("Somerset");
		city.setMap("0, 0");
		city.setHotels(new LinkedHashSet<Hotel>());

		Hotel hotel = new Hotel();
		hotel.setId(9L);
		hotel.setName("The Bath Priory Hotel (Assis)");
		hotel.setAddress("Weston Road");
		hotel.setZip("BA1 2XT");
		city.getHotels().add(hotel);

		city = this.cityRepository.saveOne(city);
		this.entityManager.flush();
		Assertions.assertNotNull(city);
		Assertions.assertEquals(1, city.getHotels().size());
	}

	@Test
	void test08() {

		City city = new City();
		city.setId(9L);
		city.setCountry("UK");
		city.setName("Bath (NEW)");
		city.setState("Somerset");
		city.setMap("0, 0");

		Hotel hotel = new Hotel();
		hotel.setName("Marcelao Plaza Hotel");
		hotel.setAddress("Weston Road");
		hotel.setZip("BA1 2XT");
		hotel.setCity(city);

		hotel = this.hotelRepository.saveOne(hotel);
		this.entityManager.flush();
		Assertions.assertNotNull(hotel);
		Assertions.assertEquals(Long.valueOf(9L), hotel.getCity().getId());
		// Não pode atualizar o hotel por conta do cascade
		Assertions.assertEquals("Bath", hotel.getCity().getName());
	}

}
