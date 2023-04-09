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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.Entity;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import tech.go9.spine.web.api.usecase.GetControllerHandlerMethod;
import tech.go9.spine.web.api.usecase.ResolveDomainClass;
import tech.go9.spine.core.api.exception.UnexpectedException;

@Component
@AllArgsConstructor
public class DefaultResolveDomainClass implements ApplicationContextAware, ResolveDomainClass {

	private GetControllerHandlerMethod getControllerMethodHandler;

	private Map<String, Class<?>> domainMap;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		EntityScanner entityScanner = new EntityScanner(applicationContext);
		try {
			this.domainMap = entityScanner.scan(Entity.class).stream()
					.collect(Collectors.toMap(Class::getSimpleName, entityClass -> entityClass));
		}
		catch (ClassNotFoundException exception) {
			throw new UnexpectedException(exception);
		}
	}

	@Override
	public Optional<Class<?>> execute() {
		return this.getControllerMethodHandler.execute()
				.map(handlerMethod -> this.getDomainClass(handlerMethod.getBeanType()));
	}

	private Class<?> getDomainClass(Class<?> controllerClass) {
		return Arrays.asList(controllerClass.getInterfaces()).stream()
				.filter(c -> c.getSimpleName().endsWith("Controller")).findFirst().map(Class::getSimpleName)
				.map(interfaceName -> interfaceName.substring(0, interfaceName.lastIndexOf("Controller")))
				.map(this.domainMap::get)
				.orElseThrow(() -> new UnexpectedException("Domain class not found for controller %s",
						controllerClass.getName()));
	}

}
