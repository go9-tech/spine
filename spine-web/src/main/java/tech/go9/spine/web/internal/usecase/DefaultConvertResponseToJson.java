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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import tech.go9.spine.web.api.usecase.ConvertResponseToOutput;
import tech.go9.spine.core.api.exception.UnexpectedException;

@Component
public class DefaultConvertResponseToJson extends MappingJackson2HttpMessageConverter
		implements ConvertResponseToOutput {

	public DefaultConvertResponseToJson(@Qualifier("objectMapper") ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	protected JavaType getJavaType(Type type, Class<?> contextClass) {
		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			if (parameterizedType.getRawType().getTypeName().equals(Page.class.getName())) {
				Class<?> domainClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
				TypeFactory typeFactory = this.defaultObjectMapper.getTypeFactory();
				return (JavaType) typeFactory.constructCollectionLikeType(Page.class, domainClass);
			}
		}
		return super.getJavaType(type, contextClass);
	}

	@Override
	protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		super.writeInternal(object, type, outputMessage);
	}

	@Override
	public void execute(Object output, OutputStream outputStream) {
		try {
			this.defaultObjectMapper.writeValue(outputStream, output);
		}
		catch (Exception exception) {
			throw new UnexpectedException(exception);
		}
	}

}
