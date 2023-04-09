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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;

import tech.go9.spine.web.api.exception.InvalidSortExpressionException;
import tech.go9.spine.web.internal.usecase.DefaultParseSort;
import tech.go9.spine.sample.api.domain.City;
import tech.go9.spine.sample.api.domain.Review;
import tech.go9.spine.sample.api.repository.CityRepository;
import tech.go9.spine.sample.api.repository.ReviewRepository;
import tech.go9.spine.web.internal.configuration.SpineWebTestConfiguration;

@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(classes = { SpineWebTestConfiguration.class })
class DefaultSortParserTest {

	@Autowired
	private DefaultParseSort sortParser;

	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	@Test
	void testGetEntitySort00() {
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20));
		Assertions.assertTrue(page.getSort().isUnsorted());
	}

	@Test
	void testGetEntitySort01() {
		String sorterExpression = "name desc";
		Sort sort = this.sortParser.execute(sorterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20, sort));
		Assertions.assertTrue(page.getSort().isSorted());
	}

	@Test
	void testGetEntitySort02() {
		try {
			String sorterExpression = "name SBLAUB";
			Sort sort = this.sortParser.execute(sorterExpression);
			cityRepository.findAll(PageRequest.of(0, 20, sort));
		}
		catch (InvalidSortExpressionException e) {
			Assertions.assertTrue(e.getMessage().contains("Invalid order syntax for part"));
		}
	}

	@Test
	void testEntirySort03() {
		String sorterExpression = "name asc, id desc";
		Sort sort = this.sortParser.execute(sorterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20, sort));
		Assertions.assertTrue(page.getSort().isSorted());
	}

	@Test
	void testEntirySort04() {
		String sorterExpression = "name";
		Sort sort = this.sortParser.execute(sorterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20, sort));
		Assertions.assertTrue(page.getSort().isSorted());
	}

	@Test
	void testEntirySort05() {
		String sorterExpression = "name, id";
		Sort sort = this.sortParser.execute(sorterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20, sort));
		Assertions.assertTrue(page.getSort().isSorted());
	}

	@Test
	void testGetEntitySort06() {
		String sorterExpression = "state asc";
		Sort sort = this.sortParser.execute(sorterExpression);
		Page<City> page = cityRepository.findAll(PageRequest.of(0, 20, sort));
		Assertions.assertTrue(page.getSort().isSorted());
	}

	@Test
	void testGetEntitySort07() {
		String sorterExpression = "checkInDate asc";
		Sort sort = this.sortParser.execute(sorterExpression);
		Page<Review> page = reviewRepository.findAll(PageRequest.of(0, 20, sort));
		Assertions.assertTrue(page.getSort().isSorted());
	}

}
