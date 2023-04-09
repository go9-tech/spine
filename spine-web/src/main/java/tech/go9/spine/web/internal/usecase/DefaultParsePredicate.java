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

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.SimplePath;

import lombok.AllArgsConstructor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import tech.go9.spine.web.api.exception.InvalidFilterExpressionException;
import tech.go9.spine.web.api.function.PredicateVisitor;
import tech.go9.spine.web.api.usecase.ParsePredicate;
import tech.go9.spine.web.internal.utils.QuerydslUtils;
import tech.go9.spine.core.api.util.ApplicationContextUtils;

@Component
@AllArgsConstructor
public class DefaultParsePredicate implements ParsePredicate {

	@Override
	public Predicate execute(Class<?> type, String expression) {

		Assert.notNull(type, "Parameter type must not be null");
		Assert.notNull(expression, "Parameter expression must not be null");

		String sanitizedExpression = this.sanitizeExpression(expression);
		Expression jsqlExpression;
		try {
			jsqlExpression = CCJSqlParserUtil.parseCondExpression(sanitizedExpression);
		}
		catch (JSQLParserException jsqlParserException) {
			throw new InvalidFilterExpressionException(jsqlParserException.getMessage(), jsqlParserException);
		}

		PredicateVisitor predicateVisitor = ApplicationContextUtils.getBean(PredicateVisitor.class).orElseThrow();
		SimplePath<?> rootPath = QuerydslUtils.createRootPath(type);
		predicateVisitor.init(rootPath);
		jsqlExpression.accept(predicateVisitor);
		return predicateVisitor.getPredicate();
	}

	private String sanitizeExpression(String expression) {
		if (expression.startsWith("\"") && expression.endsWith("\"")) {
			return expression.substring(1, expression.length() - 1);
		}
		else {
			return expression;
		}
	}

}
