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
package tech.go9.spine.web.api.function;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.SimplePath;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;

/**
 * This class is responsible to define the visitor methods used to parse the entity filter
 * expression.
 *
 * @since 1.0.0
 * @author thiago.assis
 */
public interface PredicateVisitor extends ExpressionVisitor, ItemsListVisitor {

	void init(final SimplePath<?> rootPath);

	/**
	 * @return a predicate to filter implementation.
	 */
	Predicate getPredicate();

}
