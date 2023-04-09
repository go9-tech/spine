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
package tech.go9.spine.web.internal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import lombok.AllArgsConstructor;
import tech.go9.spine.web.api.controller.OpenApiController;
import tech.go9.spine.web.api.domain.OpenApi;
import tech.go9.spine.web.api.usecase.RetrieveOpenApi;
import tech.go9.spine.web.internal.configuration.SpineWebProperties;

@Controller
@AllArgsConstructor
public class DefaultOpenApiController implements OpenApiController {

	private final RetrieveOpenApi retrieveOpenApi;

	private final SpineWebProperties properties;

	@Override
	public String getOpenApi(Integer version, Model model) {
		OpenApi openApi = this.retrieveOpenApi.execute();
		model.addAttribute("title", openApi.getTitle());
		model.addAttribute("version", openApi.getVersion());
		model.addAttribute("description", openApi.getDescription());
		model.addAttribute("serverUrl", openApi.getServerUrl().toString());
		model.addAttribute("openIdConnectUrl", properties.getOpenIdConnectUrl());
		model.addAttribute("openIdConnectIssuer", properties.getOpenIdConnectIssuer());
		model.addAttribute("openIdConnectScope", properties.getOpenIdConnectScope());
		return String.format("openapi-v%s", version);
	}

}
