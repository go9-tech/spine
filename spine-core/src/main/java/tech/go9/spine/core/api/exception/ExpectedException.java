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
package tech.go9.spine.core.api.exception;

import java.util.Optional;

import org.springframework.util.Assert;

import lombok.Getter;
import lombok.Setter;
import tech.go9.spine.core.internal.util.MessageUtils;

@Getter
@Setter
public class ExpectedException extends Exception implements ManagedException {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_STATUS = "500";

	private static final String DEFAULT_CODE = "UNKNOW";

	private static final Optional<Object> DEFAULT_META = Optional.empty();

	private final String status;

	private final String code;

	private final Optional<Object> meta;

	public ExpectedException() {
		super();
		status = DEFAULT_STATUS;
		code = DEFAULT_CODE;
		meta = DEFAULT_META;
	}

	public ExpectedException(String message, Throwable cause) {
		super(message, cause);
		status = DEFAULT_STATUS;
		code = DEFAULT_CODE;
		meta = DEFAULT_META;
	}

	public ExpectedException(String message, Throwable cause, Object... args) {
		super(String.format(message, args), cause);
		status = DEFAULT_STATUS;
		code = DEFAULT_CODE;
		meta = DEFAULT_META;
	}

	public ExpectedException(String message) {
		super(message);
		status = DEFAULT_STATUS;
		code = DEFAULT_CODE;
		meta = DEFAULT_META;
	}

	public ExpectedException(String message, Object... args) {
		super(String.format(message, args));
		status = DEFAULT_STATUS;
		code = DEFAULT_CODE;
		meta = DEFAULT_META;
	}

	public ExpectedException(Throwable cause) {
		super(cause);
		status = DEFAULT_STATUS;
		code = DEFAULT_CODE;
		meta = DEFAULT_META;
	}

	public ExpectedException(String status, String bundle, String code, Optional<Object> meta) {
		super(MessageUtils.getString(bundle, code));
		Assert.notNull(status, "status must not be null");
		Assert.notNull(code, "code must not be null");
		this.status = status;
		this.code = code.toUpperCase();
		this.meta = meta;
	}

	public ExpectedException(String status, String bundle, String code, Optional<Object> meta, Object... args) {
		super(String.format(MessageUtils.getString(bundle, code), args));
		Assert.notNull(status, "status must not be null");
		Assert.notNull(code, "code must not be null");
		this.status = status;
		this.code = code.toUpperCase();
		this.meta = meta;
	}

	public ExpectedException(String status, String bundle, String code, Optional<Object> meta, Throwable cause) {
		super(MessageUtils.getString(bundle, code), cause);
		Assert.notNull(status, "status must not be null");
		Assert.notNull(code, "code must not be null");
		this.status = status;
		this.code = code.toUpperCase();
		this.meta = meta;
	}

	public ExpectedException(String status, String bundle, String code, Optional<Object> meta, Throwable cause,
			Object... args) {
		super(String.format(MessageUtils.getString(bundle, code), args), cause);
		Assert.notNull(status, "status must not be null");
		Assert.notNull(code, "code must not be null");
		this.status = status;
		this.code = code.toUpperCase();
		this.meta = meta;
	}

}
