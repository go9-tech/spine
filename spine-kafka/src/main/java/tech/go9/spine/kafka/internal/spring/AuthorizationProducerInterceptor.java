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
package tech.go9.spine.kafka.internal.spring;

import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import tech.go9.spine.kafka.internal.configuration.SpineKafkaConstants;

public class AuthorizationProducerInterceptor<K, V> implements ProducerInterceptor<K, V> {

	@Override
	public void configure(Map<String, ?> configs) {
		// TODO Auto-generated method stub
	}

	@Override
	public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
		this.getJwt().ifPresent(jwt -> {
			String value = String.format(SpineKafkaConstants.AUTHORIZATION_FORMAT, jwt.getTokenValue());
			record.headers().add(SpineKafkaConstants.AUTHORIZATION_HEADER, value.getBytes());
		});
		return record;
	}

	@Override
	public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}

	private Optional<Jwt> getJwt() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext != null) {
			Authentication authentication = securityContext.getAuthentication();
			if (authentication != null && authentication.isAuthenticated()
					&& authentication instanceof JwtAuthenticationToken) {
				JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) securityContext
						.getAuthentication();
				Jwt jwt = jwtAuthenticationToken.getToken();
				return Optional.ofNullable(jwt);
			}
		}
		return Optional.empty();
	}

}
