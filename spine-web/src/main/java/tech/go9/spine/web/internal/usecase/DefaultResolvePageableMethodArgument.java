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
import java.util.Optional;
import java.util.OptionalInt;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import lombok.AllArgsConstructor;
import tech.go9.spine.web.api.usecase.ParsePageable;
import tech.go9.spine.web.internal.configuration.SpineWebConstants;

@Component
@AllArgsConstructor
public class DefaultResolvePageableMethodArgument implements HandlerMethodArgumentResolver, InitializingBean {

	private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

	private final ParsePageable pageableParser;

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
		return methodParameter.getParameterType().equals(Pageable.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		return this.pageableParser.execute(getPageNumber(webRequest), getPageSize(webRequest), getSort(webRequest));
	}

	private OptionalInt getPageNumber(NativeWebRequest webRequest) {
		String pageNumber = webRequest.getParameter(SpineWebConstants.PAGE_NUMBER_REQUEST_PARAM_NAME);
		if (pageNumber == null || pageNumber.isBlank()) {
			return OptionalInt.empty();
		}
		else {
			return OptionalInt.of(Integer.parseInt(pageNumber));
		}
	}

	private OptionalInt getPageSize(NativeWebRequest webRequest) {
		String pageSize = webRequest.getParameter(SpineWebConstants.PAGE_SIZE_REQUEST_PARAM_NAME);
		if (pageSize == null || pageSize.isBlank()) {
			return OptionalInt.empty();
		}
		else {
			return OptionalInt.of(Integer.parseInt(pageSize));
		}
	}

	private Optional<String> getSort(NativeWebRequest webRequest) {
		return Optional.ofNullable(webRequest.getParameter(SpineWebConstants.SORT_REQUEST_PARAM_NAME));
	}

}
