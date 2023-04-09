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
package tech.go9.spine.web.client.api.jackson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;

public class PageDeserializer extends JsonDeserializer<Page<?>> implements ContextualDeserializer {

	private static final String CONTENT = "content";

	private static final String NUMBER = "number";

	private static final String SIZE = "size";

	private static final String TOTAL_ELEMENTS = "totalElements";

	private JavaType valueType;

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
			throws JsonMappingException {
		// This is the Page actually
		final JavaType wrapperType = ctxt.getContextualType();
		final PageDeserializer deserializer = new PageDeserializer();
		// This is the parameter of Page
		deserializer.valueType = wrapperType.containedType(0);
		return deserializer;
	}

	@Override
	public Page<?> deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {

		final CollectionType valuesListType = context.getTypeFactory().constructCollectionType(List.class, valueType);

		List<?> list = new ArrayList<>();
		int pageNumber = 0;
		int pageSize = 0;
		long total = 0;
		if (jsonParser.isExpectedStartObjectToken()) {
			jsonParser.nextToken();
			if (jsonParser.hasTokenId(JsonTokenId.ID_FIELD_NAME)) {
				String propName = jsonParser.getCurrentName();
				do {
					jsonParser.nextToken();
					switch (propName) {
						case CONTENT:
							list = context.readValue(jsonParser, valuesListType);
							break;
						case NUMBER:
							pageNumber = context.readValue(jsonParser, Integer.class);
							break;
						case SIZE:
							pageSize = context.readValue(jsonParser, Integer.class);
							break;
						case TOTAL_ELEMENTS:
							total = context.readValue(jsonParser, Long.class);
							break;
						default:
							jsonParser.skipChildren();
							break;
					}
				}
				while (((propName = jsonParser.nextFieldName())) != null);
			}
			else {
				context.handleUnexpectedToken(handledType(), jsonParser);
			}
		}
		else {
			context.handleUnexpectedToken(handledType(), jsonParser);
		}

		// Note that Sort field of Page is ignored here.
		// Feel free to add more switch cases above to deserialize it as well.
		return new PageImpl<>(list, PageRequest.of(pageNumber, pageSize), total);
	}

}
