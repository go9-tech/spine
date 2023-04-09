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
package tech.go9.spine.web.internal.utils;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.CaseFormat;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimplePath;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import tech.go9.spine.core.api.util.ReflectionUtils;

public final class QuerydslUtils {

	private static Map<Class<?>, SimplePath<?>> rootPathMap = new HashMap<>();

	// private static Map<String, PathBuilder<?>> pathBuilderMap = new HashMap<>();

	private QuerydslUtils() {

	}

	public static <N> SimplePath<N> createRootPath(Class<N> type) {
		SimplePath<?> rootPath = QuerydslUtils.rootPathMap.get(type);
		if (rootPath == null) {
			rootPath = Expressions.path(type, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, type.getSimpleName()));
			QuerydslUtils.rootPathMap.put(type, rootPath);
		}
		return (SimplePath<N>) rootPath;
	}

	public static Path<?> createPath(SimplePath<?> rootPath, String attributePath) {
		if (attributePath.contains(".")) {
			return createCompositePath(rootPath, attributePath);
		}
		else {
			return createSimplePath(rootPath, attributePath);
		}
	}

	private static Path<?> createSimplePath(SimplePath<?> rootPath, String attributePath) {
		Class<?> rootType = rootPath.getType();
		Field field = ReflectionUtils.findField(rootType, attributePath);
		Class fieldType = field.getType();
		if (fieldType.isEnum()) {
			return Expressions.enumPath(fieldType, rootPath, attributePath);
		}
		else if (fieldType.getName().equals("java.net.URI")) {
			return Expressions.path(URI.class, rootPath, attributePath);
		}
		else {
			return Expressions.path(String.class, rootPath, attributePath);
		}
	}

	private static Path<?> createCompositePath(SimplePath<?> rootPath, String attributePath) {

		List<String> attributeNames = new LinkedList<>(Arrays.asList(attributePath.split("\\.")));
		Class<?> type = rootPath.getType();
		String attributeName = rootPath.getMetadata().getName();
		PathBuilder<?> pathBuilder = new PathBuilder(type, attributeName);
		do {

			attributeName = attributeNames.remove(0);
			Field field = ReflectionUtils.findField(type, attributeName);
			if (field == null) {
				throw new IllegalStateException(
						String.format("Class %s does not contain field name %s", type.getName(), attributeName));
			}

			if (Collection.class.isAssignableFrom(field.getType())) {

				type = ReflectionUtils.getCollectionType(field);
				if (field.getType().isAssignableFrom(Set.class)) {
					pathBuilder = pathBuilder.getSet(attributeName, type).any();
				}
				else if (field.getType().isAssignableFrom(List.class)) {
					pathBuilder = pathBuilder.getList(attributeName, type).any();
				}
				else if (field.getType().isAssignableFrom(Collection.class)) {
					pathBuilder = pathBuilder.getCollection(attributeName, type).any();
				}
				else {
					throw new RuntimeException(String.format("Unknown collections type %s", field.getType()));
				}

			}
			else {

				type = field.getType();
				if (type.isEnum()) {
					return getEnumPath(type, pathBuilder, attributeName);
				}
				else if (type.getName().equals("java.net.URI")) {
					return Expressions.path(URI.class, rootPath, attributePath);
				}
				else {
					pathBuilder = pathBuilder.get(attributeName);
				}
			}

		}
		while (!attributeNames.isEmpty());

		return pathBuilder;
	}

	private static EnumPath getEnumPath(Class type, Path<?> path, String name) {
		return Expressions.enumPath(type, path, name);
	}

	public static Expression<String> getParameter(Path<?> rootPath, ExpressionList parameterList, int number) {

		if (parameterList == null || parameterList.getExpressions().isEmpty()) {
			throw new IllegalArgumentException("Parameter list is empty");
		}

		net.sf.jsqlparser.expression.Expression expression = parameterList.getExpressions().get(number);
		if (expression instanceof Column) {
			return Expressions.path(String.class, rootPath, ((Column) expression).getColumnName());
		}

		String strParam = expression.toString().trim();
		if (strParam.startsWith("'") && strParam.endsWith("'")) {
			strParam = strParam.substring(1, strParam.length() - 1);
		}

		return Expressions.constant(strParam);
	}

}
