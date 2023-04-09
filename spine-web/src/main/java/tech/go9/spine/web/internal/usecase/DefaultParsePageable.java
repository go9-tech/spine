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
package tech.go9.spine.web.internal.usecase;

import java.util.Optional;
import java.util.OptionalInt;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import lombok.AllArgsConstructor;
import tech.go9.spine.web.api.usecase.ParsePageable;
import tech.go9.spine.web.api.usecase.ParseSort;
import tech.go9.spine.web.internal.configuration.SpineWebConstants;
import tech.go9.spine.web.internal.configuration.SpineWebProperties;

@Component
@AllArgsConstructor
public class DefaultParsePageable implements ParsePageable {

	private final ParseSort sortParser;

	private final SpineWebProperties properties;

	@Override
	public Pageable execute(OptionalInt opageNumber, OptionalInt pageSize, Optional<String> expression) {

		Assert.notNull(opageNumber, "Parameter opageNumber must not be null");
		Assert.notNull(pageSize, "Parameter pageSize must not be null");
		Assert.notNull(expression, "Parameter expression must not be null");

		if (expression.isPresent()) {

			return PageRequest.of(opageNumber.orElse(SpineWebConstants.DEFAULT_PAGE_NUMBER),
					pageSize.orElse(this.properties.getDefaultPageSize()), this.sortParser.execute(expression.get()));

		}
		else {

			return PageRequest.of(opageNumber.orElse(SpineWebConstants.DEFAULT_PAGE_NUMBER),
					pageSize.orElse(this.properties.getDefaultPageSize()));
		}
	}

}
