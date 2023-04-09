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

import jakarta.servlet.DispatcherType;

import org.hashids.Hashids;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.github.sett4.dataformat.xlsx.XlsxMapper;

@Configuration
@ComponentScan(SpineWebConstants.APP_BASE_PACKAGE)
@EnableConfigurationProperties(SpineWebProperties.class)
public class SpineWebConfiguration {

	@Bean
	Hashids Hashids() {
		return new Hashids("alpe_rules!");
	}

	@Bean
	CsvMapper csvMapper(List<Module> modules) {
		CsvMapper objectMapper = new CsvMapper();
		this.configureMapper(objectMapper, modules);
		return objectMapper;
	}

	@Bean
	XlsxMapper xlsxMapper(List<Module> modules) {
		XlsxMapper objectMapper = new XlsxMapper();
		this.configureMapper(objectMapper, modules);
		return objectMapper;
	}

	// https://tomgregory.com/spring-boot-behind-load-balancer-using-x-forwarded-headers/
	@Bean
	FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
		ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
		FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		registration.setUrlPatterns(List.of("/"));
		return registration;
	}

	@Bean
	ITemplateResolver templateResolver() {
		ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setPrefix("/templates/");
		templateResolver.setSuffix(".yaml");
		templateResolver.setCharacterEncoding("UTF-8");
		return templateResolver;
	}

	private void configureMapper(ObjectMapper objectMapper, List<Module> modules) {
		modules.forEach(objectMapper::registerModule);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

}
