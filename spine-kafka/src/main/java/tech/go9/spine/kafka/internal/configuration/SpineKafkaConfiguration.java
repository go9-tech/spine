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
package tech.go9.spine.kafka.internal.configuration;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import tech.go9.spine.kafka.internal.spring.AuthorizationProducerInterceptor;
import tech.go9.spine.kafka.internal.spring.AuthorizationRecordInterceptor;

@Configuration
@EnableConfigurationProperties(SpineKafkaProperties.class)
@ComponentScan(SpineKafkaConstants.BASE_PACKAGE)
@AutoConfigureBefore({ KafkaAutoConfiguration.class })
@EnableKafka
public class SpineKafkaConfiguration {

	@Bean
	Serializer<String> keySerializer() {
		return new StringSerializer();
	}

	@Bean
	Serializer<Object> valueSerializer(ObjectMapper objectMapper) {
		return new JsonSerializer<>(objectMapper);
	}

	@Bean
	Deserializer<String> keyDeserializer() {
		return new StringDeserializer();
	}

	@Bean
	Deserializer<Object> valueDeserializer(ObjectMapper objectMapper) {
		JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>(objectMapper);
		ErrorHandlingDeserializer<Object> valueDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);
		valueDeserializer.setForKey(false);
		valueDeserializer.setFailedDeserializationFunction(new Function<FailedDeserializationInfo, Object>() {
			@Override
			public Object apply(FailedDeserializationInfo failedDeserializationInfo) {
				byte[] data = failedDeserializationInfo.getData();
				return new String(data);
			}
		});
		return valueDeserializer;
	}

	@Bean
	ConsumerFactory<?, ?> kafkaConsumerFactory(KafkaProperties properties,
			ObjectProvider<DefaultKafkaConsumerFactoryCustomizer> customizers, Deserializer<String> keyDeserializer,
			Deserializer<Object> valueDeserializer) {
		DefaultKafkaConsumerFactory<String, Object> factory = new DefaultKafkaConsumerFactory<>(
				properties.buildConsumerProperties(), keyDeserializer, valueDeserializer);
		customizers.orderedStream().forEach((customizer) -> customizer.customize(factory));
		return factory;
	}

	@Bean
	ProducerFactory<?, ?> kafkaProducerFactory(KafkaProperties properties, Serializer<String> keySerializer,
			Serializer<Object> valueSerializer, ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {
		Map<String, Object> producerProperties = properties.buildProducerProperties();
		producerProperties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
				AuthorizationProducerInterceptor.class.getName());
		DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(producerProperties, keySerializer,
				valueSerializer);
		String transactionIdPrefix = properties.getProducer().getTransactionIdPrefix();
		if (transactionIdPrefix != null) {
			factory.setTransactionIdPrefix(transactionIdPrefix);
		}
		customizers.orderedStream().forEach((customizer) -> customizer.customize(factory));
		return factory;
	}

	@Bean
	DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaOperations<Object, Object> operations) {
		return new DeadLetterPublishingRecoverer(operations,
				(consumerRecord, exception) -> new TopicPartition(consumerRecord.topic() + ".dlt", 0));
	}

	@Bean
	DefaultErrorHandler defaultErrorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
		DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterPublishingRecoverer);
		errorHandler.addNotRetryableExceptions(SerializationException.class);
		return errorHandler;
	}

	@Bean
	<K, V> ConcurrentKafkaListenerContainerFactory<K, V> concurrentKafkaListenerContainerFactoryConfigurer(
			ConcurrentKafkaListenerContainerFactory<K, V> concurrentKafkaListenerContainerFactory,
			AuthorizationRecordInterceptor<K, V> authorizationRecordInterceptor,
			DefaultErrorHandler defaultErrorHandler) {
		concurrentKafkaListenerContainerFactory.setRecordInterceptor(authorizationRecordInterceptor);
		concurrentKafkaListenerContainerFactory.setCommonErrorHandler(defaultErrorHandler);
		return concurrentKafkaListenerContainerFactory;
	}

	@Bean
	public KafkaAdmin kafkaAdmin(KafkaProperties properties, Environment environment) {
		Map<String, Object> adminProperties = properties.buildAdminProperties();
		KafkaAdmin kafkaAdmin = new KafkaAdmin(adminProperties);
		kafkaAdmin.setFatalIfBrokerNotAvailable(true);
		boolean autoCreate = Arrays.stream(environment.getActiveProfiles())
				.anyMatch(env -> env.equalsIgnoreCase("test") || env.equalsIgnoreCase("default"));
		kafkaAdmin.setAutoCreate(autoCreate);
		return kafkaAdmin;
	}

}
