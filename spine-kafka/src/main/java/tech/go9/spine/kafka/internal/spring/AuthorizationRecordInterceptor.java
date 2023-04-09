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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.core.convert.converter.Converter;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.go9.spine.core.api.exception.UnexpectedException;
import tech.go9.spine.kafka.api.usecase.RefreshToken;

@Slf4j
@Component
@AllArgsConstructor
public class AuthorizationRecordInterceptor<K, V> implements RecordInterceptor<K, V> {

	private final Optional<JwtDecoder> jwtDecoder;

	private final Optional<Converter<Jwt, AbstractAuthenticationToken>> jwtBearerTokenAuthenticationConverter;

	private final RefreshToken refreshToken;

	private static final int TIME_EXCHANGE_SECONDS = 10;

	@Override
	public ConsumerRecord<K, V> intercept(ConsumerRecord<K, V> consumerRecord, Consumer<K, V> consumer) {
		this.getAuthenticationHeader(consumerRecord).ifPresent(this::initializeSecurityContext);
		return consumerRecord;
	}

	@Override
	public void failure(ConsumerRecord<K, V> record, Exception exception, Consumer<K, V> consumer) {

		try {

			StringBuilder message = new StringBuilder();
			message.append("Error consuming record from topic ");
			message.append(record.topic());
			if (log.isInfoEnabled()) {
				message.append(" with key ");
				message.append(record.key());
				message.append(" and value ");
				message.append(record.value());
			}

			log.error(message.toString(), exception);

		}
		catch (Throwable throwable) {

			StringBuilder message = new StringBuilder();
			message.append("Error consuming record from topic ");
			message.append(record.topic());
			exception.addSuppressed(throwable);
			log.error(message.toString(), exception);
		}
	}

	private Optional<Header> getAuthenticationHeader(ConsumerRecord<K, V> consumerRecord) {
		try {
			return Optional.ofNullable(consumerRecord.headers().headers("Authorization").iterator().next());
		}
		catch (NoSuchElementException e) {
			return Optional.empty();
		}
	}

	private void initializeSecurityContext(Header header) {
		String accessToken = this.getAccessToken(header);
		try {
			Jwt jwt = this.jwtDecoder.orElseThrow(() -> new UnexpectedException("JwtDecoder bean not found"))
					.decode(accessToken);
			Instant expiry = jwt.getExpiresAt();
			Duration clockSkew = Duration.ofSeconds(TIME_EXCHANGE_SECONDS);
			if (expiry != null && Instant.now().minus(clockSkew).isAfter(expiry)) {
				jwt = getRefreshToken(accessToken);
			}
			setSecureContext(jwt);
		}
		catch (JwtValidationException iex) {
			if (iex.getMessage().contains("expired")) {
				Jwt refreshToken = getRefreshToken(accessToken);
				setSecureContext(refreshToken);
			}
			else {
				throw iex;
			}
		}
	}

	private Jwt getRefreshToken(String accessToken) {
		String jwt = this.refreshToken.execute(accessToken);
		return this.jwtDecoder.orElseThrow(() -> new UnexpectedException("JwtDecoder bean not found")).decode(jwt);
	}

	private void setSecureContext(Jwt jwt) {
		Authentication authentication = this.jwtBearerTokenAuthenticationConverter
				.orElseThrow(
						() -> new UnexpectedException("Converter<Jwt, AbstractAuthenticationToken> bean not found"))
				.convert(jwt);
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	private String getAccessToken(Header header) {
		String authentication = new String(header.value(), StandardCharsets.UTF_8);
		return authentication.split(" ")[1];
	}

}
