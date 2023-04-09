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
package tech.go9.spine.web.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import tech.go9.spine.core.api.exception.ManagedException;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class Error {

	private String code;

	private String message;

	private Object meta;

	public static Error from(Throwable throwable) {
		Error error = new Error();
		error.setCode(getCode(throwable));
		error.setMessage(getMessage(throwable));
		error.setMeta(getMeta(throwable));
		return error;
	}

	private static String getCode(Throwable throwable) {
		if (ManagedException.class.isAssignableFrom(throwable.getClass())) {
			return ((ManagedException) throwable).getCode();
		}
		else {
			return null;
		}
	}

	private static String getMessage(Throwable throwable) {
		String message = throwable.getMessage();
		if (message != null && !message.isBlank()) {
			return throwable.getMessage();
		}
		else {
			return null;
		}
	}

	private static Object getMeta(Throwable throwable) {
		if (ManagedException.class.isAssignableFrom(throwable.getClass())) {
			return ((ManagedException) throwable).getMeta();
		}
		else {
			return null;
		}
	}

}
