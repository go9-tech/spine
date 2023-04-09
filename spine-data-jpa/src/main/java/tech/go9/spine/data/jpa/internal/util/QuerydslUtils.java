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
package tech.go9.spine.data.jpa.internal.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import com.google.common.base.CaseFormat;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimplePath;

public final class QuerydslUtils {

	private static final EntityPathResolver entityPathResolver = SimpleEntityPathResolver.INSTANCE;

	private static final Map<String, EntityPath<?>> entityPathMap = new HashMap<>();

	private static final Map<String, PathBuilder<?>> pathBuilderMap = new HashMap<>();

	private QuerydslUtils() {

	}

	public static <T> SimplePath<T> createRootPath(Class<T> clazz) {
		return Expressions.path(clazz, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, clazz.getSimpleName()));
	}

	public static <T, I> SimplePath<I> createIdPath(JpaEntityInformation<T, I> entityInformation,
			SimplePath<T> rootPath) {
		return Expressions.path(entityInformation.getIdType(), rootPath, entityInformation.getIdAttribute().getName());
	}

}
