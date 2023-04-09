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
package tech.go9.spine.data.jpa.internal.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;

import tech.go9.spine.data.jpa.internal.component.DefaultAuditorAware;

@Configuration
@EnableConfigurationProperties(SpineDataJpaProperties.class)
@EnableTransactionManagement
@EnableJpaAuditing
public class SpineDataJpaConfiguration {

	@Bean
	Hibernate5JakartaModule hibernate5Module() {
		Hibernate5JakartaModule hibernate5Module = new Hibernate5JakartaModule();
		hibernate5Module.disable(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING);
		hibernate5Module.disable(Hibernate5JakartaModule.Feature.USE_TRANSIENT_ANNOTATION);
		return hibernate5Module;
	}

	/*
	 * @Bean Module spineDataJpaModule() { SimpleModule spineDataJpaModule = new
	 * SimpleModule(); spineDataJpaModule.addSerializer(HibernateProxy.class, new
	 * CustomHibernateProxySerializer());
	 * spineDataJpaModule.addSerializer(ByteBuddyInterceptor.class, new
	 * ByteBuddyInterceptorJacksonSerializer()); return spineDataJpaModule; }
	 */

	@Bean
	AuditorAware<String> auditorAware() {
		return new DefaultAuditorAware();
	}

}
