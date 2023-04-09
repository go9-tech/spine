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
package tech.go9.spine.core.internal.configuration;

import java.time.Instant;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import tech.go9.spine.core.api.jackson.CustomInstantDeserializer;
import tech.go9.spine.core.api.usecase.GetMessage;
import tech.go9.spine.core.api.util.ApplicationContextSetter;
import tech.go9.spine.core.internal.usecase.DefaultGetMessage;

@Configuration
public class SpineCoreConfiguration {

	@Bean
	ApplicationContextSetter applicationContextSetter() {
		return new ApplicationContextSetter();
	}

	@Bean
	GetMessage getMessage() {
		return new DefaultGetMessage();
	}

	@Bean
	JavaTimeModule javaTimeModule() {
		return new JavaTimeModule();
	}

	@Bean
	Jdk8Module jdk8Module() {
		return new Jdk8Module();
	}

	/*
	 * @Bean JaxbAnnotationModule jaxbAnnotationModule() { return new
	 * JaxbAnnotationModule(); }
	 */

	@Bean
	Module spineCoreModule() {
		SimpleModule customModule = new SimpleModule();
		customModule.addDeserializer(Instant.class, new CustomInstantDeserializer());
		return customModule;
	}

	@Bean("objectMapper")
	ObjectMapper objectMapper(List<Module> modules) {
		ObjectMapper objectMapper = new ObjectMapper();
		this.configureMapper(objectMapper, modules);
		return objectMapper;
	}

	@Bean("yamlMapper")
	ObjectMapper yamlMapper(List<Module> modules) {
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		this.configureMapper(objectMapper, modules);
		return objectMapper;
	}

	@Bean
	XmlMapper xmlMapper(List<Module> modules) {
		XmlMapper objectMapper = new XmlMapper();
		this.configureMapper(objectMapper, modules);
		return objectMapper;
	}

	private void configureMapper(ObjectMapper objectMapper, List<Module> modules) {
		modules.forEach(objectMapper::registerModule);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

}
