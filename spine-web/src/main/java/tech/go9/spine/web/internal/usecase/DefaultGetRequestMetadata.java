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

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import tech.go9.spine.web.api.domain.RequestMetadata;
import tech.go9.spine.web.api.usecase.GetRequestMetadata;
import tech.go9.spine.web.internal.configuration.SpineWebConstants;

@Component
@ConditionalOnWebApplication
public class DefaultGetRequestMetadata implements GetRequestMetadata {

	@Override
	public Optional<RequestMetadata> execute() {
		ServletRequestAttributes requestAttributes = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes());
		if (requestAttributes != null) {
			HttpServletRequest httpServletRequest = requestAttributes.getRequest();
			RequestMetadata requestMetadata = (RequestMetadata) httpServletRequest
					.getAttribute(SpineWebConstants.REQUEST_METADATA_PARAMETER);
			return Optional.ofNullable(requestMetadata);
		}
		else {
			return Optional.empty();
		}
	}

}
