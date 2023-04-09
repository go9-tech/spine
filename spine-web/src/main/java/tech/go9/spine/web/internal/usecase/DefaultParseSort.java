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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import tech.go9.spine.web.api.exception.InvalidSortExpressionException;
import tech.go9.spine.web.api.usecase.ParseSort;

@Component
public class DefaultParseSort implements ParseSort {

	private static final String SORT_EXPRESSION_SEPARATOR_CHAR = ",";

	private static final Pattern SORT_EXPRESSION_PART_PATTERN = Pattern.compile("(.+?)(asc|desc)?$");

	private static final Integer ONE = 1;

	private static final Integer TWO = 2;

	@Override
	public Sort execute(String sortExpression) {
		return Sort.by(getOrders(sortExpression));
	}

	private List<Order> getOrders(String expression) {

		Assert.notNull(expression, "Parameter expression must not be null");

		List<Order> orders = new ArrayList<>();
		for (String sortExpressionPart : expression.split(SORT_EXPRESSION_SEPARATOR_CHAR)) {

			Matcher matcher = SORT_EXPRESSION_PART_PATTERN.matcher(sortExpressionPart);
			if (!matcher.find()) {
				throw new InvalidSortExpressionException(sortExpressionPart, expression);
			}

			String propertyString = matcher.group(ONE);
			String directionString = matcher.group(TWO);
			if (sortExpressionPart.trim().split(" ").length > 1 && directionString == null) {
				throw new InvalidSortExpressionException(sortExpressionPart, expression);
			}

			if (directionString == null || directionString.trim().isEmpty() || directionString.trim().equals("asc")) {
				orders.add(Order.asc(propertyString.trim()));
			}
			else if (directionString.trim().equals("desc")) {
				orders.add(Order.desc(propertyString.trim()));
			}
			else {
				throw new InvalidSortExpressionException(sortExpressionPart, expression);
			}

		}

		return orders;
	}

}
