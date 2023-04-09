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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hashids.Hashids;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import lombok.AllArgsConstructor;

//@Component
@AllArgsConstructor
public class DefaultResolveIdMethodArgument implements HandlerMethodArgumentResolver, InitializingBean {

	private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

	private final Hashids hashids;

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

		PathVariable pathVariable = methodParameter.getParameterAnnotation(PathVariable.class);
		if (pathVariable == null) {
			return false;
		}

		String parameterName = methodParameter.getParameterName();
		if ((pathVariable.value() != null
				&& (pathVariable.value().equals("id") || pathVariable.value().startsWith("idOr")))
				|| parameterName.equals("id") || parameterName.startsWith("idOr")) {
			return true;
		}

		return false;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		String pathVariableValue = this.getPathVariableValue(webRequest);
		if (pathVariableValue.startsWith("uri:")) {
			return pathVariableValue;
		}
		return String.valueOf(this.hashids.decode(pathVariableValue)[0]);
	}

	// https://stackoverflow.com/questions/51921635/how-to-get-requests-uri-from-webrequest-in-spring
	private String getPathVariableValue(NativeWebRequest webRequest) {
		return "";
	}

}
