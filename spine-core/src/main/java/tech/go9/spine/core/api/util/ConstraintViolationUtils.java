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

import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;

public final class ConstraintViolationUtils {

	private ConstraintViolationUtils() {

	}

	public static <T> ConstraintViolation<T> of(T object, String path, String message, Object... args) {
		String messageTemplate = getMessageTemplate(message);
		Map<String, Object> messageParameters = getMessageParameters(args);
		return ConstraintViolationImpl.forParameterValidation(getMessageTemplate(message), getMessageParameters(args),
				getExpressionVariables(), getInterpolatedMessage(messageTemplate, messageParameters),
				getRootClass(object), object, getLeafBeanInstance(), getValue(), getPropertyPath(),
				getConstraintDescriptor(), getExecutableParameters(), getDynamicPayload());
	}

	private static String getMessageTemplate(String basicTemplate) {
		String messageTemplate = basicTemplate;
		return messageTemplate;
	}

	private static Map<String, Object> getMessageParameters(Object... args) {
		Map<String, Object> messageParameters = new HashMap<>();
		return messageParameters;
	}

	private static Map<String, Object> getExpressionVariables(Object... args) {
		Map<String, Object> messageParameters = new HashMap<>();
		return messageParameters;
	}

	private static String getInterpolatedMessage(String messageTemplate, Map<String, Object> messageParameters) {
		return messageTemplate;
	}

	private static <T> Class<T> getRootClass(T object) {
		return (Class<T>) object.getClass();
	}

	private static Object getLeafBeanInstance() {
		return null;
	}

	private static Object getValue() {
		return null;
	}

	private static Path getPropertyPath() {
		return null;
	}

	private static ConstraintDescriptor<?> getConstraintDescriptor() {
		return null;
	}

	private static Object[] getExecutableParameters() {
		return null;
	}

	private static Object getDynamicPayload() {
		return null;
	}

	/*
	 * public static <T, A extends Annotation> ConstraintViolation<T> forField( final T
	 * rootBean, final Class<T> clazz, final Class<A> annotationClazz, final Object
	 * leafBean, final String field, final Object offendingValue) {
	 *
	 * ConstraintViolation<T> violation = null; try { Field member =
	 * clazz.getDeclaredField(field); A annotation =
	 * member.getAnnotation(annotationClazz); ConstraintDescriptor<A> descriptor = new
	 * ConstraintDescriptorImpl<>( new ConstraintHelper(), member, annotation,
	 * ElementType.FIELD); Path p = PathImpl.createPathFromString(field); violation =
	 * ConstraintViolationImpl.forBeanValidation( MESSAGE_TEMPLATE, MESSAGE, clazz,
	 * rootBean, leafBean, offendingValue, p, descriptor, ElementType.FIELD); } catch
	 * (NoSuchFieldException ignore) {} return violation;
	 *
	 * }
	 */

}
