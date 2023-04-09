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

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import tech.go9.spine.core.api.exception.MultipleException;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ErrorMessage {

	@JsonInclude(Include.NON_NULL)
	private List<Error> errors;

	public static ErrorMessage from(Throwable throwable) {

		List<Error> errors = new ArrayList<>();
		if (ConstraintViolationException.class.isAssignableFrom(throwable.getClass())) {

			ConstraintViolationException constraintViolationException = (ConstraintViolationException) throwable;
			addConstraintViolation(constraintViolationException, errors);

		}
		else if (MultipleException.class.isAssignableFrom(throwable.getClass())) {

			MultipleException multipleException = (MultipleException) throwable;
			List<Throwable> throwables = multipleException.getThrowables();
			throwables.forEach(t -> addThrowable(t, errors));

		}
		else {

			addThrowable(throwable, errors);
		}

		ErrorMessage errorMessage = new ErrorMessage();
		errorMessage.setErrors(errors);
		return errorMessage;
	}

	private static void addThrowable(Throwable throwable, List<Error> errors) {
		errors.add(Error.from(throwable));
		Throwable cause = throwable.getCause();
		if (cause != null) {
			addThrowable(cause, errors);
		}
		if (throwable.getSuppressed() != null) {
			for (Throwable suppressed : throwable.getSuppressed()) {
				addThrowable(suppressed, errors);
			}
		}
	}

	private static void addConstraintViolation(ConstraintViolationException constraintViolationException,
			List<Error> errors) {
		constraintViolationException.getConstraintViolations()
				.forEach(constraintViolation -> addConstraintViolation(constraintViolation, errors));
	}

	private static void addConstraintViolation(ConstraintViolation<?> constraintViolation, List<Error> errors) {
		Error error = new Error();
		String message = String.format("%s: %s", constraintViolation.getPropertyPath(),
				constraintViolation.getMessage());
		error.setMessage(message);
		errors.add(error);
	}

}
