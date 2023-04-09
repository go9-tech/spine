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
package tech.go9.spine.sample.internal.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import tech.go9.spine.sample.api.domain.Hotel;
import tech.go9.spine.sample.api.repository.HotelRepository;
import tech.go9.spine.sample.api.usecase.RetrieveHotels;

@Service
@AllArgsConstructor
public class DefaultRetrieveHotels implements RetrieveHotels {

	private final HotelRepository HotelRepository;

	@Override
	public Page<Hotel> execute() {
		return this.HotelRepository.findAll(PageRequest.of(0, 10));
	}

}
