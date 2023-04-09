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
package tech.go9.spine.core.api.jackson;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

/**
 * JSON deserializer customizado para atributos Instant serem convertidos tanto pelo
 * formato ISO_INSTANT (UTC) como com ISO_OFFSET_DATE_TIME.
 *
 */
public class CustomInstantDeserializer extends StdScalarDeserializer<Instant> {

	private static final long serialVersionUID = 1L;

	public CustomInstantDeserializer() {
		super(Instant.class);
	}

	@Override
	public Instant deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {

		String string = parser.getText().trim();

		if (string.toUpperCase().endsWith("Z")) {
			return InstantDeserializer.INSTANT.deserialize(parser, context);
		}

		OffsetDateTime offsetDate = InstantDeserializer.OFFSET_DATE_TIME.deserialize(parser, context);
		return offsetDate.toInstant();
	}

}
