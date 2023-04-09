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
package tech.go9.spine.web.internal.spring;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import lombok.extern.slf4j.Slf4j;
import tech.go9.spine.web.api.domain.ErrorMessage;
import tech.go9.spine.core.api.exception.ExpectedException;
import tech.go9.spine.core.api.exception.ManagedException;
import tech.go9.spine.core.api.exception.MultipleException;
import tech.go9.spine.core.api.exception.UnexpectedException;
import jakarta.validation.ValidationException;

@Slf4j
@ControllerAdvice
@ConditionalOnWebApplication
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler implements ResponseBodyAdvice {

	@ExceptionHandler({ MultipleException.class, ExpectedException.class, UnexpectedException.class, Throwable.class })
	public final ResponseEntity<Object> handleExceptionCustom(Throwable throwable, WebRequest request) {
		HttpHeaders headers = new HttpHeaders();
		HttpStatusCode httpStatusCode = this.getHttpStatusCode(throwable);
		return this.handleExceptionInternalCustom(throwable, null, headers, httpStatusCode, request);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		// TODO Auto-generated method stub
		return this.handleExceptionInternalCustom(exception, body, headers, statusCode, request);
	}

	protected ResponseEntity<Object> handleExceptionInternalCustom(Throwable throwable, Object body,
			HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

		if (statusCode.is5xxServerError()) {
			log.error(throwable.getMessage(), throwable);
		}

		if (HttpStatus.INTERNAL_SERVER_ERROR.equals(statusCode)) {
			request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, throwable, WebRequest.SCOPE_REQUEST);
		}

		ErrorMessage errorMessage = ErrorMessage.from(throwable);
		return new ResponseEntity<>(errorMessage, headers, statusCode);
	}

	protected HttpStatusCode getHttpStatusCode(Throwable throwable) {

		if (throwable instanceof ManagedException) {
			ManagedException ex = (ManagedException) throwable;
			return HttpStatus.valueOf(Integer.parseInt(ex.getStatus()));
		}
		else if (throwable instanceof MultipleException) {
			return HttpStatus.BAD_REQUEST;
		}
		else if (throwable instanceof ResponseStatusException) {
			return ((ResponseStatusException) throwable).getStatusCode();
		}
		else if (throwable instanceof ValidationException) {
			return HttpStatus.BAD_REQUEST;
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public boolean supports(MethodParameter returnType, Class converterType) {
		return returnType.getParameterType().equals(Optional.class);
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
		if (returnType.getParameterType().equals(Optional.class)) {
			return ((Optional<?>) body).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		}
		return body;
	}

}
