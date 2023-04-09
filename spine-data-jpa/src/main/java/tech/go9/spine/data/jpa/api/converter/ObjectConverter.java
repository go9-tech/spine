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
package tech.go9.spine.data.jpa.api.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tech.go9.spine.core.api.util.ApplicationContextUtils;

@Converter
public class ObjectConverter implements AttributeConverter<Object, String> {

	private ObjectMapper objectMapper;

	@Override
	public String convertToDatabaseColumn(Object attribute) {

		if (attribute == null) {
			return null;
		}

		try {
			return attribute.getClass().getName() + "|" + this.getObjectMapper().writeValueAsString(attribute);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Error converting to database column", e);
		}
	}

	@Override
	public Object convertToEntityAttribute(String dbData) {

		if (dbData == null || dbData.isBlank()) {
			return null;
		}

		try {
			final String[] parts = dbData.split("\\|", 2);
			return this.getObjectMapper().readValue(parts[1], Class.forName(parts[0]));
		}
		catch (Exception e) {
			throw new RuntimeException("Error converting to database attribute", e);
		}
	}

	private ObjectMapper getObjectMapper() {
		if (this.objectMapper == null) {
			this.objectMapper = ApplicationContextUtils.getApplicationContext().getBean("objectMapper",
					ObjectMapper.class);
		}
		return this.objectMapper;
	}

}
