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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.querydsl.core.types.Predicate;

import tech.go9.spine.core.api.util.ApplicationContextUtils;
import tech.go9.spine.data.jpa.api.tenancy.TenancyFilter;

public final class EntityTenancyFilterUtils {

	private static Map<String, TenancyFilter<?>> entityTenancyFilterMap;

	private EntityTenancyFilterUtils() {

	}

	public static <T> Optional<Predicate> getRetrievePredicate(Class<T> entityClazz) {
		TenancyFilter<T> entityTenancyFilter = getEntityTenancyFilter(entityClazz);
		if (entityTenancyFilter != null) {
			return entityTenancyFilter.getRetrievePredicate();
		}
		else {
			return Optional.empty();
		}
	}

	public static <T> Optional<Predicate> getSavePredicate(Class<T> entityClazz) {
		TenancyFilter<T> entityTenancyFilter = getEntityTenancyFilter(entityClazz);
		if (entityTenancyFilter != null) {
			return entityTenancyFilter.getSavePredicate();
		}
		else {
			return Optional.empty();
		}
	}

	public static <T> Optional<Predicate> getDeletePredicate(Class<T> entityClazz) {
		TenancyFilter<T> entityTenancyFilter = getEntityTenancyFilter(entityClazz);
		if (entityTenancyFilter != null) {
			return entityTenancyFilter.getDeletePredicate();
		}
		else {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> TenancyFilter<T> getEntityTenancyFilter(Class<T> entityClazz) {
		if (EntityTenancyFilterUtils.entityTenancyFilterMap == null) {
			loadEntityTenancyFilters();
		}
		return (TenancyFilter<T>) entityTenancyFilterMap.get(entityClazz.getName());
	}

	private static void loadEntityTenancyFilters() {
		EntityTenancyFilterUtils.entityTenancyFilterMap = new HashMap<>();
		Map<String, TenancyFilter<?>> entityTenancyFilterMap = ApplicationContextUtils.getBeans(TenancyFilter.class);
		for (Entry<String, TenancyFilter<?>> entityTenancyFilterMapEntry : entityTenancyFilterMap.entrySet()) {
			TenancyFilter<?> entityTenancyFilter = entityTenancyFilterMapEntry.getValue();
			getType(entityTenancyFilter.getClass()).ifPresent(type -> EntityTenancyFilterUtils.entityTenancyFilterMap
					.put(type.getTypeName(), entityTenancyFilter));
		}
	}

	private static Optional<Type> getType(Class<?> clazz) {
		Type[] types = clazz.getGenericInterfaces();
		for (Type type : types) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			if (parameterizedType.getRawType().getTypeName().equals(TenancyFilter.class.getName())) {
				return Optional.of(parameterizedType.getActualTypeArguments()[0]);
			}
		}
		Class<?> superClazz = clazz.getSuperclass();
		if (superClazz != null) {
			return getType(superClazz);
		}
		return Optional.empty();
	}

}
