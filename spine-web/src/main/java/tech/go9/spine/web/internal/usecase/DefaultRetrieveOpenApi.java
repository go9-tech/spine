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
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.go9.spine.web.api.domain.OpenApi;
import tech.go9.spine.web.api.usecase.RetrieveOpenApi;
import tech.go9.spine.web.internal.configuration.SpineWebConstants;
import tech.go9.spine.web.internal.configuration.SpineWebProperties;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultRetrieveOpenApi implements RetrieveOpenApi {

	private final SpineWebProperties properties;

	@Override
	public OpenApi execute() {
		return this.getFromBuildProperties().orElseGet(this::getFromSpineProperties);
	}

	private Optional<OpenApi> getFromBuildProperties() {

		try (InputStream inputStream = RetrieveOpenApi.class.getClassLoader()
				.getResourceAsStream(SpineWebConstants.BUILD_INFO_PROPERTIES_PATH)) {

			Properties properties = new Properties();
			properties.load(inputStream);

			OpenApi openApi = this.properties.getOpenApi();
			if (openApi == null) {
				openApi = new OpenApi();
			}

			if (openApi.getTitle() == null) {
				openApi.setTitle(properties.getProperty("build.name", "Unknown"));
			}

			if (openApi.getVersion() == null) {
				openApi.setVersion(properties.getProperty("build.version", "Unknown"));
			}

			if (openApi.getDescription() == null) {
				openApi.setDescription(properties.getProperty("build.description", "Unknown"));
			}

			if (openApi.getServerUrl() == null || openApi.getServerUrl().toString().isBlank()) {
				openApi.setServerUrl(this.getServerUrl());
			}

			return Optional.of(openApi);

		}
		catch (IOException exception) {
			log.debug("File {} not found in classpath", SpineWebConstants.BUILD_INFO_PROPERTIES_PATH);
			return Optional.empty();
		}
	}

	private OpenApi getFromSpineProperties() {

		OpenApi openApi = this.properties.getOpenApi();
		if (openApi == null) {
			openApi = new OpenApi();
		}

		if (openApi.getTitle() == null) {
			openApi.setTitle("Unknown");
		}

		if (openApi.getVersion() == null) {
			openApi.setVersion("Unknown");
		}

		if (openApi.getDescription() == null) {
			openApi.setDescription("Unknown");
		}

		if (openApi.getServerUrl() == null || openApi.getServerUrl().toString().isBlank()) {
			openApi.setServerUrl(this.getServerUrl());
		}

		return openApi;
	}

	// https://tomgregory.com/spring-boot-behind-load-balancer-using-x-forwarded-headers/
	private URI getServerUrl() {
		UriComponents serverurl = ServletUriComponentsBuilder.fromCurrentRequest().build();
		String path = serverurl.getPath();
		int index = path.indexOf("/api/v");
		if (index <= 0) {
			index = 1;
		}
		path = path.substring(1, index);
		String scheme = serverurl.getScheme();
		int port = serverurl.getPort();
		String authority;
		if (port > 0 && ((scheme.equals("https") && port != 443) || (scheme.equals("http") && port != 80))) {
			authority = String.format("%s:%s", serverurl.getHost(), serverurl.getPort());
		}
		else {
			authority = serverurl.getHost();
		}
		String serverUrl = String.format("%s://%s/%s", serverurl.getScheme(), authority, path);
		return URI.create(serverUrl);
	}

}
