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
package tech.go9.spine.web.api.exception;

/**
 * This class represents exceptions that are thrown during the entity sort expression
 * parsing process.
 *
 * @since 1.0.0
 * @author thiago.assis
 */
public class InvalidSortExpressionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static final String INVALID_ORDER_SYNTAX = "Invalid order syntax for part: %s of: %s";

	public InvalidSortExpressionException(String sortExpressionPart, String expression) {
		super(String.format(INVALID_ORDER_SYNTAX, sortExpressionPart, expression));
	}

}
