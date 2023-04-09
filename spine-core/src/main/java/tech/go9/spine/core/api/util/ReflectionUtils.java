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
package tech.go9.spine.core.api.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeanUtils;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import tech.go9.spine.core.api.exception.UnexpectedException;

public final class ReflectionUtils {

	private static final Map<Class<?>, Map<String, Field>> fieldsMap = new ConcurrentHashMap<>();

	private static final Map<String, Optional<Pair<Method, Method>>> getterAndSetterMap = new ConcurrentHashMap<>();

	private ReflectionUtils() {

	}

	public static <N> N newInstance(Class<N> clazz) {
		try {
			return clazz.getConstructor().newInstance();
		}
		catch (Exception exception) {
			throw new UnexpectedException(
					"Unable to instantiate class %s, verify if that class have an empty constructor", clazz.getName());
		}
	}

	public static <N> Field findField(Class<N> clazz, String name) {
		Map<String, Field> fields = fieldsMap.get(clazz);
		if (fields == null) {
			fields = fildAllFields(clazz);
		}
		return fields.get(name);
	}

	public static <N> Map<String, Field> fildAllFields(Class<N> clazz) {
		Map<String, Field> fields = fieldsMap.get(clazz);
		if (fields == null) {
			fields = new ConcurrentHashMap<>();
			Class<?> superClazz = clazz.getSuperclass();
			if (superClazz != null) {
				fields.putAll(fildAllFields(superClazz));
			}
			for (Field field : clazz.getDeclaredFields()) {
				fields.put(field.getName(), field);
			}
			fieldsMap.put(clazz, fields);
		}
		return fields;
	}

	/*
	 * public static Map<String, Field> getFieldsMap(Object object) { Map<String, Field>
	 * fields = fieldsMap.get(object.getClass()); if (fields == null) { fields = new
	 * HashMap<>(); for (Field field : object.getClass().getDeclaredFields()) {
	 * fields.put(field.getName(), field); } } return fields; }
	 *
	 * public static Field getField(Object object, String name) { Map<String, Field>
	 * fields = getFieldsMap(object); Field field = fields.get(name); if (field == null) {
	 * throw new UnexpectedException( String.format("Unable to get field %s.%s",
	 * object.getClass().getName(), name)); } return field; }
	 *
	 * public static Object getFieldValue(Object object, Field field) { try { if
	 * (!field.isAccessible()) { field.setAccessible(true); } return field.get(object); }
	 * catch (Exception exception) { throw new UnexpectedException(
	 * String.format("Unable to get field %s.%s value", object.getClass().getName(),
	 * field.getName())); } }
	 */

	public static Object getFieldValue(Object object, Field field)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return getFieldValue(object, field, false);
	}

	public static Object getFieldValue(Object object, Field field, boolean force)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		Optional<Pair<Method, Method>> getterAndSetterPair = findGetterAndSetter(object, field);
		if (getterAndSetterPair.isPresent()) {
			Method method = getterAndSetterPair.get().getFirst();
			if (method != null) {
				return method.invoke(object);
			}
		}
		if (force) {
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			return field.get(object);
		}
		throw new NoSuchMethodException(String.format("Getter method for field %s in class %s not found",
				field.getName(), object.getClass().getName()));
	}

	public static void setFieldValue(Object object, Field field, Object value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		setFieldValue(object, field, value, false);
	}

	public static void setFieldValue(Object object, Field field, Object value, boolean force)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Optional<Pair<Method, Method>> getterAndSetterPair = findGetterAndSetter(object, field);
		if (getterAndSetterPair.isPresent()) {
			Method method = getterAndSetterPair.get().getSecond();
			if (method != null) {
				method.invoke(object, value);
				return;
			}
		}
		if (force) {
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			field.set(object, value);
			return;
		}
		throw new NoSuchMethodException(String.format("Setter method for field %s in class %s not found",
				field.getName(), object.getClass().getName()));
	}

	public static Optional<Pair<Method, Method>> findGetterAndSetter(Object object, Field field) {
		String getterSetterKey = object.getClass().getCanonicalName() + "." + field.getName();
		return getterAndSetterMap.computeIfAbsent(getterSetterKey, k -> findGetterAndSetter(object.getClass(), field));
	}

	private static Optional<Pair<Method, Method>> findGetterAndSetter(Class<?> clazz, Field field) {

		Method getter = null;
		Method setter = null;
		PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(clazz, field.getName());
		if (propertyDescriptor == null) {
			Class<?> superClazz = clazz.getSuperclass();
			if (superClazz != null) {
				return findGetterAndSetter(superClazz, field);
			}
		}

		getter = propertyDescriptor.getReadMethod();
		setter = propertyDescriptor.getWriteMethod();
		if (getter != null && setter != null) {
			return Optional.of(Pair.of(getter, setter));
		}
		else {
			return Optional.empty();
		}
	}

	public static <N> Class<N> getCollectionType(Field field) {
		Assert.notNull(field, "Field must not be null!");
		if (Collection.class.isAssignableFrom(field.getType())) {
			if (field.getGenericType() instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
				return (Class<N>) parameterizedType.getActualTypeArguments()[0];
			}
			else {
				throw new MalformedParameterizedTypeException();
			}
		}
		else {
			throw new ClassCastException(String.format("Field %s type is not assignable from %s", field.getName(),
					Collection.class.getName()));
		}
	}

	public static <N> Class<N> getClass(N object) {
		return (Class<N>) object.getClass();
	}

}
