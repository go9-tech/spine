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
package tech.go9.spine.data.jpa.internal.component;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import tech.go9.spine.core.api.util.ApplicationContextUtils;
import tech.go9.spine.core.api.util.ReflectionUtils;
import tech.go9.spine.data.jpa.internal.configuration.SpineDataJpaProperties;

@Slf4j
public class TenancyEntityListener {

	@PrePersist
	public void touchForCreate(Object target) {
		this.setField(target);
	}

	@PreUpdate
	public void touchForUpdate(Object target) {
		this.setField(target);
	}

	private Optional<String> getFieldName() {
		return ApplicationContextUtils.getBean(SpineDataJpaProperties.class)
				.map(properties -> properties.getTenancyFieldName())
				.filter(tenancyFieldName -> !tenancyFieldName.equals("undefined"));
	}

	private void setField(Object target) {
		this.getField(target)
				.ifPresent(field -> this.getValue().ifPresent(value -> this.setValue(target, field, value)));
	}

	private Optional<Field> getField(Object target) {
		return this.getFieldName().map(fieldName -> ReflectionUtils.findField(target.getClass(), fieldName));
	}

	private Optional<URI> getValue() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext != null) {
			Authentication authentication = securityContext.getAuthentication();
			if (authentication != null) {
				return Optional.of(URI.create(authentication.getName()));
			}
		}
		return Optional.empty();
	}

	private void setValue(Object target, Field field, Object value) {
		try {
			ReflectionUtils.setFieldValue(target, field, value);
		}
		catch (Exception exception) {
			log.error("Error setting entity {} field {}", target.getClass(), field.getName());
		}
	}

}
