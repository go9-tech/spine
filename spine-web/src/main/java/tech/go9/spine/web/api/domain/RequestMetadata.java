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

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.Getter;

@Getter
public class RequestMetadata {

	private String id;

	private URI resource;

	private Instant instant;

	private String user;

	public RequestMetadata() {

	}

	public RequestMetadata(HttpServletRequest httpServletRequest) {
		this.id = UUID.randomUUID().toString();
		this.resource = URI.create(httpServletRequest.getRequestURL()
				.append(Optional.ofNullable(httpServletRequest.getQueryString()).orElse("")).toString());
		this.instant = Instant.now();
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext != null) {
			Authentication authentication = securityContext.getAuthentication();
			if (authentication != null) {
				this.user = authentication.getName();
			}
		}
	}

}
