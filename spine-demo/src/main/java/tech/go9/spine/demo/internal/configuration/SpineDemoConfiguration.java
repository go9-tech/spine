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
package tech.go9.spine.demo.internal.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import tech.go9.spine.web.internal.configuration.SpineWebConfiguration;
import tech.go9.spine.data.jpa.internal.configuration.SpineDataJpaConfiguration;
import tech.go9.spine.data.jpa.internal.repository.DefaultGenericRepository;

@Configuration
@EnableAutoConfiguration
@Import({ SpineDataJpaConfiguration.class, SpineWebConfiguration.class })
@EnableJpaRepositories(basePackages = "tech.go9.spine.demo.api.repository",
		repositoryBaseClass = DefaultGenericRepository.class)
@EntityScan("tech.go9.spine.demo.api.domain")
@ComponentScan("tech.go9.spine.demo")
public class SpineDemoConfiguration {

}
