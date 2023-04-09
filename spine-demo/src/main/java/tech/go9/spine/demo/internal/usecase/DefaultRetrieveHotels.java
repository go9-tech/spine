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
package tech.go9.spine.demo.internal.usecase;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import tech.go9.spine.demo.api.domain.Hotel;
import tech.go9.spine.demo.api.repository.HotelRepository;
import tech.go9.spine.demo.api.usecase.RetrieveHotels;

@Service
@AllArgsConstructor
public class DefaultRetrieveHotels implements RetrieveHotels {

	private final HotelRepository hotelRepository;

	@Override
	public Page<Hotel> execute(Optional<String> expand) {
		// Page<Hotel> page = hotelRepository.findAll(PageRequest.of(0, 100),
		// "city,reviews");
		if (expand.isPresent()) {
			return this.hotelRepository.findAll(PageRequest.of(0, 28), expand.get());
		}
		return this.hotelRepository.findAll(PageRequest.of(0, 28));
		// return page;
	}

}
