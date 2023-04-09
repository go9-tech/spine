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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.engine.spi.SelfDirtinessTracker;

import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import tech.go9.spine.core.api.exception.UnexpectedException;
import tech.go9.spine.core.api.util.ReflectionUtils;

public class EntityUtils {

	private static final Map<Class<?>, Field> idMap = new HashMap<>();

	private static final Map<Class<?>, List<Field>> nonRelationalFieldMap = new HashMap<>();

	private static final Map<Class<?>, List<Field>> oneToManyFieldMap = new HashMap<>();

	private static final Map<Class<?>, List<Field>> manyToManyFieldMap = new HashMap<>();

	private static final Map<Class<?>, List<Field>> oneToOneFieldMap = new HashMap<>();

	private static final Map<Class<?>, List<Field>> manyToOneFieldMap = new HashMap<>();

	private EntityUtils() {

	}

	public static <N> Field getIdField(Class<N> entityClazz) {

		if (entityClazz == null) {
			return null;
		}

		Field idField = idMap.get(entityClazz);
		if (idField != null) {
			return idField;
		}

		for (Field field : entityClazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Id.class)) {
				idField = field;
				idMap.put(entityClazz, idField);
				break;
			}
		}

		if (idField == null && entityClazz.getSuperclass() != Object.class) {
			idField = getIdField(entityClazz.getSuperclass());
		}

		return idField;
	}

	public static <N, I> I getIdValue(N entity)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Field field = getIdField(entity.getClass());
		return (I) ReflectionUtils.getFieldValue(entity, field, true);
	}

	public static <N> Optional<N> findEntity(Collection<N> collection, N entity)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (collection != null && !collection.isEmpty()) {
			Object referenceId = getIdValue(entity);
			for (N currentEntity : collection) {
				Object currentId = getIdValue(currentEntity);
				if (currentEntity == entity || (referenceId != null && currentId != null
						&& (currentId == referenceId || currentId.equals(referenceId)))) {
					return Optional.of(currentEntity);
				}
			}
		}
		return Optional.empty();
	}

	public static <N> List<Field> getOneToManyFields(final N entity) {
		return getOneToManyFields(entity.getClass());
	}

	public static List<Field> getOneToManyFields(Class<?> entityClazz) {
		List<Field> fields = oneToManyFieldMap.get(entityClazz);
		if (fields != null) {
			return fields;
		}
		else {
			fields = new ArrayList<>();
			oneToManyFieldMap.put(entityClazz, fields);
		}
		for (Field field : ReflectionUtils.fildAllFields(entityClazz).values()) {
			if (field.isAnnotationPresent(OneToMany.class) && !field.getName().equals("serialVersionUID")) {
				fields.add(field);
			}
		}
		return fields;
	}

	public static <N> List<Field> getManyToManyFields(final N entity) {
		return getManyToManyFields(entity.getClass());
	}

	public static List<Field> getManyToManyFields(Class<?> entityClazz) {
		List<Field> fields = manyToManyFieldMap.get(entityClazz);
		if (fields != null) {
			return fields;
		}
		else {
			fields = new ArrayList<>();
			manyToManyFieldMap.put(entityClazz, fields);
		}
		for (Field field : ReflectionUtils.fildAllFields(entityClazz).values()) {
			if (field.isAnnotationPresent(ManyToMany.class) && !field.getName().equals("serialVersionUID")) {
				fields.add(field);
			}
		}
		return fields;
	}

	public static <N> List<Field> getOneToOneFields(final N entity) {
		return getOneToOneFields(entity.getClass());
	}

	public static List<Field> getOneToOneFields(Class<?> entityClazz) {
		List<Field> fields = oneToOneFieldMap.get(entityClazz);
		if (fields != null) {
			return fields;
		}
		else {
			fields = new ArrayList<>();
			oneToOneFieldMap.put(entityClazz, fields);
		}
		for (Field field : ReflectionUtils.fildAllFields(entityClazz).values()) {
			if (field.isAnnotationPresent(OneToOne.class) && !field.getName().equals("serialVersionUID")) {
				fields.add(field);
			}
		}
		return fields;
	}

	public static <N> List<Field> getManyToOneFields(final N entity) {
		return getManyToOneFields(entity.getClass());
	}

	public static List<Field> getManyToOneFields(Class<?> entityClazz) {
		List<Field> fields = manyToOneFieldMap.get(entityClazz);
		if (fields != null) {
			return fields;
		}
		else {
			fields = new ArrayList<>();
			manyToOneFieldMap.put(entityClazz, fields);
		}
		for (Field field : ReflectionUtils.fildAllFields(entityClazz).values()) {
			if (field.isAnnotationPresent(ManyToOne.class) && !field.getName().equals("serialVersionUID")) {
				fields.add(field);
			}
		}
		return fields;
	}

	public static <N> List<Field> getNonRelationalFields(final N entity) {
		return getNonRelationalFields(entity.getClass());
	}

	public static List<Field> getNonRelationalFields(Class<?> entityClazz) {

		List<Field> fields = nonRelationalFieldMap.get(entityClazz);
		if (fields != null) {
			return fields;
		}
		else {
			fields = new ArrayList<>();
			nonRelationalFieldMap.put(entityClazz, fields);
		}

		ReflectionUtils
				.fildAllFields(entityClazz).values().stream().filter(field -> !isRelationalField(field)
						&& !field.isAnnotationPresent(Id.class) && !Modifier.isStatic(field.getModifiers()))
				.map(fields::add).collect(Collectors.toList());

		return fields;
	}

	public static boolean isRelationalField(Field field) {
		return field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(OneToOne.class)
				|| field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(ManyToMany.class);
	}

	public static Collection<Field> getDirtyFields(SelfDirtinessTracker selfDirtinessTracker) {
		Collection<Field> fields = new ArrayList<>();
		String[] dirtyAttributes = selfDirtinessTracker.$$_hibernate_getDirtyAttributes();
		if (dirtyAttributes != null) {
			for (String attributeName : selfDirtinessTracker.$$_hibernate_getDirtyAttributes()) {
				Field field = ReflectionUtils.findField(selfDirtinessTracker.getClass(), attributeName);
				if (!field.isAnnotationPresent(Id.class) && !EntityUtils.isRelationalField(field)) {
					fields.add(field);
				}
			}
		}
		return fields;
	}

	public static void setMappedByFields(Object fromEntity, Field field, Object toEntity, boolean setNull) {
		if (field.isAnnotationPresent(OneToMany.class)) {
			setOneToManyMappedByFields(fromEntity, field, toEntity, setNull);
		}
		else if (field.isAnnotationPresent(OneToOne.class)) {
			setOneToOneMappedByFields(fromEntity, field, toEntity, setNull);
		}
		else if (field.isAnnotationPresent(ManyToOne.class)) {
			setManyToOneMappedByFields(fromEntity, field, toEntity, setNull);
		}
	}

	public static void setOneToManyMappedByFields(Object fromEntity, Field field, Object toEntity, boolean setNull) {
		OneToMany oneToMany = field.getAnnotation(OneToMany.class);
		String mappedBy = oneToMany.mappedBy();
		if (!mappedBy.isBlank()) {
			Field mappedByField = ReflectionUtils.findField(toEntity.getClass(), mappedBy);
			if (setNull) {
				setFieldValue(toEntity, mappedByField, null);
			}
			else {
				setFieldValue(toEntity, mappedByField, fromEntity);
			}
		}
	}

	public static void setOneToOneMappedByFields(Object fromEntity, Field field, Object toEntity, boolean setNull) {

		OneToOne oneToOne = field.getAnnotation(OneToOne.class);
		if (oneToOne != null) {

			String mappedBy = oneToOne.mappedBy();
			if (!mappedBy.isBlank()) {

				Field mappedByField = ReflectionUtils.findField(toEntity.getClass(), mappedBy);
				if (setNull) {
					setFieldValue(toEntity, mappedByField, null);
				}
				else {
					setFieldValue(toEntity, mappedByField, fromEntity);
				}

			}
			else {

				for (Map.Entry<String, Field> toEntityFieldEntry : ReflectionUtils.fildAllFields(toEntity.getClass())
						.entrySet()) {

					Field toEntityField = toEntityFieldEntry.getValue();
					if (toEntityField.getType().equals(fromEntity.getClass())) {

						OneToOne childOneToOne = field.getAnnotation(OneToOne.class);
						if (childOneToOne != null) {

							String childMappedBy = oneToOne.mappedBy();
							if (!mappedBy.isBlank() && field.getName().equals(childMappedBy)) {

								if (setNull) {
									setFieldValue(toEntity, toEntityField, null);
								}
								else {
									setFieldValue(toEntity, toEntityField, fromEntity);
								}
							}
						}
					}
				}
			}
		}
	}

	public static void setManyToOneMappedByFields(Object fromEntity, Field field, Object toEntity, boolean setNull) {

		ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
		if (manyToOne != null) {

			for (Map.Entry<String, Field> toEntityFieldEntry : ReflectionUtils.fildAllFields(toEntity.getClass())
					.entrySet()) {

				Field toEntityField = toEntityFieldEntry.getValue();
				if (Collection.class.isAssignableFrom(toEntityField.getType())
						&& ReflectionUtils.getCollectionType(toEntityField).equals(fromEntity.getClass())) {

					OneToMany oneToMany = toEntityField.getAnnotation(OneToMany.class);
					if (oneToMany != null) {

						String mappedBy = oneToMany.mappedBy();
						if (!mappedBy.isBlank() && field.getName().equals(mappedBy)) {
							Field mappedByField = ReflectionUtils.findField(fromEntity.getClass(), mappedBy);
							if (setNull) {
								setFieldValue(fromEntity, mappedByField, null);
							}
							else {
								setFieldValue(fromEntity, mappedByField, toEntity);
							}
							break;
						}
					}
				}
			}
		}
	}

	private static void setFieldValue(Object object, Field field, Object value) {
		try {
			ReflectionUtils.setFieldValue(object, field, value, false);
		}
		catch (Exception exception) {
			throw new UnexpectedException("Unable to set %s.%s -> %s", exception, object.getClass().getName(),
					field.getName(), value.toString());
		}
	}

}
