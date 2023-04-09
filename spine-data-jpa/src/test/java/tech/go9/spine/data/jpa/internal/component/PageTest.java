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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import tech.go9.spine.sample.api.domain.City;
import tech.go9.spine.sample.api.repository.CityRepository;
import tech.go9.spine.sample.internal.configuration.SpineDataJpaTestConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SpineDataJpaTestConfiguration.class })
@DataJpaTest
@ActiveProfiles("default")
class PageTest {

	@Autowired
	private CityRepository cityRepository;

	@Test
	void test01() {
		Page<City> page = cityRepository.findAll(PageRequest.of(2, 3), "hotels.reviews");
		Assertions.assertNotNull(page);
		Assertions.assertEquals(3, page.getSize());
		Assertions.assertEquals(21, page.getTotalElements());
	}

	@Test
	void test02() {
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20), "hotels.reviews");
		Assertions.assertNotNull(page);
		Assertions.assertEquals(20, page.getSize());
		Assertions.assertEquals(21, page.getTotalElements());
	}

	@Test
	void test03() {
		Page<City> page = cityRepository.findAll(PageRequest.of(2, 10), "hotels.reviews");
		Assertions.assertNotNull(page);
		Assertions.assertEquals(10, page.getSize());
		Assertions.assertEquals(21, page.getTotalElements());
	}

}
