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

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.Entity;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.querydsl.core.types.Predicate;

import tech.go9.spine.web.api.usecase.ParsePredicate;
import tech.go9.spine.web.internal.configuration.SpineWebConstants;
import tech.go9.spine.core.api.exception.UnexpectedException;

@Component
public class DefaultResolvePredicateMethodArgument
		implements ApplicationContextAware, InitializingBean, HandlerMethodArgumentResolver {

	private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

	private Map<String, Class<?>> entityMap;

	private final ParsePredicate parsePredicate;

	public DefaultResolvePredicateMethodArgument(RequestMappingHandlerAdapter requestMappingHandlerAdapter,
			ParsePredicate predicateParser) {
		this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
		this.parsePredicate = predicateParser;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		EntityScanner entityScanner = new EntityScanner(applicationContext);
		try {
			this.entityMap = entityScanner.scan(Entity.class).stream()
					.collect(Collectors.toMap(Class::getSimpleName, entityClass -> entityClass));
		}
		catch (ClassNotFoundException exception) {
			throw new UnexpectedException(exception);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
		List<HandlerMethodArgumentResolver> newArgumentResolvers = new LinkedList<>();
		newArgumentResolvers.add(this);
		newArgumentResolvers.addAll(argumentResolvers);
		requestMappingHandlerAdapter.setArgumentResolvers(Collections.unmodifiableList(newArgumentResolvers));
	}

	@Override
	public boolean supportsParameter(MethodParameter methodParameter) {
		return methodParameter.getParameterType().equals(Optional.class)
				&& ((ParameterizedType) methodParameter.getGenericParameterType()).getActualTypeArguments()[0]
						.equals(Predicate.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		String expression = webRequest.getParameter(SpineWebConstants.FILTER_REQUEST_PARAM_NAME);
		if (expression == null) {
			return Optional.empty();
		}
		else {
			return this.getEntityClass(parameter.getDeclaringClass())
					.map(entityClass -> this.parsePredicate.execute(entityClass, expression));
		}
	}

	private Optional<Class<?>> getEntityClass(Class<?> controllerClass) throws ClassNotFoundException {
		return Arrays.asList(controllerClass.getInterfaces()).stream()
				.filter(c -> c.getSimpleName().endsWith("Controller")).findFirst().map(Class::getSimpleName)
				.map(interfaceName -> interfaceName.substring(0, interfaceName.lastIndexOf("Controller")))
				.map(this.entityMap::get);
	}

}
