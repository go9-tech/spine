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
package tech.go9.spine.demo.internal.controller;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import tech.go9.spine.demo.api.controller.HotelController;
import tech.go9.spine.demo.api.domain.Hotel;
import tech.go9.spine.demo.api.usecase.RetrieveHotels;

@RestController
@AllArgsConstructor
@ConditionalOnSingleCandidate(HotelController.class)
public class DefaultHotelController implements HotelController {

	private final RetrieveHotels retrieveHotels;

	@Override
	public Page<Hotel> retrieve(@RequestParam("$expand") Optional<String> expand) {
		Page<Hotel> hotels = this.retrieveHotels.execute(expand);
		return hotels;
	}

}
