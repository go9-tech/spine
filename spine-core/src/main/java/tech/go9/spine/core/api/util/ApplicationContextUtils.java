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

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

public final class ApplicationContextUtils {

	private static ApplicationContext applicationContext;

	private ApplicationContextUtils() {

	}

	protected static void setApplicationContext(ApplicationContext context) {
		applicationContext = context;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static <T, I extends T> Optional<I> getBean(Class<T> clazz) {
		try {
			return Optional.ofNullable((I) applicationContext.getBean(clazz));
		}
		catch (NoSuchBeanDefinitionException exception) {
			return Optional.empty();
		}
	}

	public static <T, I extends T> Map<String, I> getBeans(Class<T> clazz) {
		return (Map<String, I>) applicationContext.getBeansOfType(clazz);
	}

}
