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
package tech.go9.spine.web.internal.component;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import tech.go9.spine.sample.api.domain.City;
import tech.go9.spine.sample.api.domain.Hotel;
import tech.go9.spine.sample.api.domain.Rating;
import tech.go9.spine.sample.api.domain.Review;
import tech.go9.spine.sample.api.domain.TripType;

//@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = { TestApplication.class})
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { TestApplication.class} )
//@SpringBootTest
//@ContextConfiguration(classes = TestApplication.class)
//@WebMvcTest()

//@SpringBootTest(classes = { TestApplication.class })
//@AutoConfigureMockMvc
class TransactionalTest {

	// @Autowired
	private MockMvc mockMvc;

	// @Autowired
	private ObjectMapper objectMapper;

	// @Test
	// @Disabled
	void test01() throws Exception {

		City city = new City();
		city.setId(9L);
		city.setCountry("UK");
		city.setName("Bath (NEW)");
		city.setState("Somerset");
		city.setMap("0, 0");

		Hotel hotel = new Hotel();
		hotel.setId(10L);
		hotel.setCity(city);
		hotel.setName("Bath Travelodge");
		hotel.setAddress("Rossiter Road, Widcombe Basin");
		hotel.setZip("BA2 4JP");

		Review review14 = new Review();
		review14.setHotel(hotel);
		review14.setIndex(14);
		review14.setTripType(TripType.FRIENDS);
		review14.setTitle("Test14");
		review14.setDetails("Test14");
		review14.setCheckInDate(Instant.now());
		review14.setRating(Rating.GOOD);

		List<Review> reviews = new ArrayList<>();
		reviews.add(review14);

		this.mockMvc
				.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(reviews)))
				.andDo(print()).andExpect(status().isCreated());
	}

	/*
	 * HttpEntity<Object> requestEntity = new HttpEntity<>(reviews, getHeaders());
	 * ResponseEntity<List<Review>> responseEntity =
	 * this.restTemplate.exchange(URI.create("http://localhost:8080/api/v1/reviews/stream"
	 * ), HttpMethod.POST, requestEntity, new ParameterizedTypeReference<List<Review>>()
	 * {}); reviews = responseEntity.getBody(); Assertions.assertNotNull(reviews);
	 */

}
