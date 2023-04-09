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

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import tech.go9.spine.demo.internal.configuration.SpineDemoConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SpineDemoConfiguration.class)
class BrunoTest {

	@LocalServerPort
	private String port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void test() {

		RequestEntity<Void> requestEntity = RequestEntity
				.method(HttpMethod.GET, URI.create(String
						.format("http://localhost:%s/api/v1/hotels?$expand=city,reviews&$format=json", this.port)))
				.build();
		ResponseEntity<JsonNode> response = this.restTemplate.exchange(requestEntity, JsonNode.class);
		System.out.println(response.getBody());

		requestEntity = RequestEntity.method(HttpMethod.GET,
				URI.create(String.format("http://localhost:%s/api/v1/hotels?$format=json", this.port))).build();
		response = this.restTemplate.exchange(requestEntity, JsonNode.class);
		System.out.println(response.getBody());

	}

}
