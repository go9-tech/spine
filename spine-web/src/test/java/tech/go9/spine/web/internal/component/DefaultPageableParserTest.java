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
import java.util.OptionalInt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;

import tech.go9.spine.web.internal.configuration.SpineWebConstants;
import tech.go9.spine.web.internal.configuration.SpineWebProperties;
import tech.go9.spine.web.internal.usecase.DefaultParsePageable;
import tech.go9.spine.web.internal.configuration.SpineWebTestConfiguration;

@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(classes = { SpineWebTestConfiguration.class })
class DefaultPageableParserTest {

	@Autowired
	private SpineWebProperties properties;

	@Autowired
	private DefaultParsePageable defaultPageableParser;

	@Test
	void test00() {
		Pageable pageable = defaultPageableParser.execute(OptionalInt.empty(), OptionalInt.empty(), Optional.empty());
		Assertions.assertEquals(SpineWebConstants.DEFAULT_PAGE_NUMBER, pageable.getPageNumber());
		Assertions.assertEquals(this.properties.getDefaultPageSize(), pageable.getPageSize());
		Assertions.assertEquals(pageable.getSort(), Sort.unsorted());
	}

	@Test
	void test01() {
		Pageable pageable = defaultPageableParser.execute(OptionalInt.of(2), OptionalInt.empty(), Optional.empty());
		Assertions.assertEquals(2, pageable.getPageNumber());
		Assertions.assertEquals(this.properties.getDefaultPageSize(), pageable.getPageSize());
		Assertions.assertEquals(pageable.getSort(), Sort.unsorted());
	}

	@Test
	void test02() {
		Pageable pageable = defaultPageableParser.execute(OptionalInt.of(10), OptionalInt.of(49), Optional.empty());
		Assertions.assertEquals(10, pageable.getPageNumber());
		Assertions.assertEquals(49, pageable.getPageSize());
		Assertions.assertEquals(pageable.getSort(), Sort.unsorted());
	}

}
