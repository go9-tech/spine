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
package tech.go9.spine.sample.internal.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

import tech.go9.spine.core.api.exception.UnexpectedException;
import tech.go9.spine.sample.api.controller.ErrorController;
import tech.go9.spine.sample.api.domain.Hotel;

@RestController
public class DefaultErrorController implements ErrorController {

	@Override
	public Page<Hotel> retrieve() {
		throw new UnexpectedException("500", "spine-web", "TEST", null, "A vitoria é muitop legal!");
	}

}
