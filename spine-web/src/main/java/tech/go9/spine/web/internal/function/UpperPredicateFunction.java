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
package tech.go9.spine.web.internal.function;

import org.springframework.stereotype.Component;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import tech.go9.spine.web.api.function.PredicateFunction;
import tech.go9.spine.web.internal.utils.QuerydslUtils;

@Component
public class UpperPredicateFunction implements PredicateFunction {

	@Override
	public StringExpression visit(Path<?> rootPath, ExpressionList parameterList) {
		return Expressions.asString(QuerydslUtils.getParameter(rootPath, parameterList, 0)).upper();
	}

}
