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

import java.io.OutputStream;

import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import tech.go9.spine.web.api.usecase.ConvertResponseToOutput;
import tech.go9.spine.core.api.exception.UnexpectedException;

@Component
public class DefaultConvertResponseToXml extends MappingJackson2XmlHttpMessageConverter
		implements ConvertResponseToOutput {

	public DefaultConvertResponseToXml(XmlMapper xmlMapper) {
		super(xmlMapper);
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
