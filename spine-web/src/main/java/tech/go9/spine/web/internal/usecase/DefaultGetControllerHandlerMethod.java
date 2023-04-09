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

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.AllArgsConstructor;
import tech.go9.spine.web.api.usecase.GetControllerHandlerMethod;
import tech.go9.spine.web.api.usecase.GetHttpServletRequest;
import tech.go9.spine.core.api.exception.UnexpectedException;

@Component
@AllArgsConstructor
public class DefaultGetControllerHandlerMethod implements GetControllerHandlerMethod {

	private final GetHttpServletRequest getHttpServletRequest;

	private final RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Override
	public Optional<HandlerMethod> execute() {
		return this.getHttpServletRequest.execute().map(this::getControllerClass);
	}

	private HandlerMethod getControllerClass(HttpServletRequest httpServletRequest) {
		try {
			return (HandlerMethod) requestMappingHandlerMapping.getHandler(httpServletRequest).getHandler();
		}
		catch (Exception e) {
			throw new UnexpectedException("Unable to determine ret controller class");
		}
	}

}
