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
package tech.go9.spine.web.internal.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import tech.go9.spine.web.internal.spring.RequestMetadataHandlerInterceptor;
import tech.go9.spine.web.internal.usecase.DefaultConvertResponseToJson;
import tech.go9.spine.web.internal.usecase.DefaultConvertResponseToXml;
import tech.go9.spine.web.internal.usecase.DefaultConvertResponseToYaml;

@Configuration
public class CustomWebMvcConfigurer implements WebMvcConfigurer {

	@Autowired
	private DefaultConvertResponseToJson convertResponseToJson;

	@Autowired
	private DefaultConvertResponseToYaml convertResponseToYaml;

	@Autowired
	private DefaultConvertResponseToXml convertResponseToXml;

	// @Autowired
	// private DefaultConvertResponseToXlsx convertResponseToXslx;

	// @Autowired
	// private DefaultConvertResponseToCsv convertResponseToCsv;

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		// @formatter:off
		configurer.ignoreUnknownPathExtensions(true)
			.favorPathExtension(true)
			.favorParameter(true)
			.parameterName("$format")
			.ignoreAcceptHeader(false)
			.useRegisteredExtensionsOnly(true)
			.defaultContentType(MediaType.APPLICATION_JSON)
			.mediaType("json", MediaType.APPLICATION_JSON)
			.mediaType("yaml", MediaType.valueOf("application/yaml"))
			.mediaType("xml", MediaType.APPLICATION_XML);
			//.mediaType("csv", MediaType.valueOf("text/csv"))
			//.mediaType("xlsx", MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
			//.mediaType("pdf", MediaType.APPLICATION_PDF);
		// @formatter:on
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(this.convertResponseToJson);
		converters.add(this.convertResponseToXml);
		converters.add(this.convertResponseToYaml);
		// converters.add(this.convertResponseToCsv);
		// converters.add(this.convertResponseToXslx);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new RequestMetadataHandlerInterceptor());
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		// registry.addRedirectViewController("/api/v{version:[0-9]+}/doc",
		// "/api/v{version}/doc/index.html");
		registry.addRedirectViewController("/api/v1/doc", "/api/v1/doc/index.html");
		registry.addRedirectViewController("/api/v2/doc", "/api/v2/doc/index.html");
		registry.addRedirectViewController("/api/v3/doc", "/api/v3/doc/index.html");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// registry.addResourceHandler("/api/v{version:[0-9]+}/doc/**").addResourceLocations("classpath:/static/swagger-ui/");
		registry.addResourceHandler("/api/v1/doc/**").addResourceLocations("classpath:/static/swagger-ui/");
		registry.addResourceHandler("/api/v2/doc/**").addResourceLocations("classpath:/static/swagger-ui/");
		registry.addResourceHandler("/api/v3/doc/**").addResourceLocations("classpath:/static/swagger-ui/");
	}

}
