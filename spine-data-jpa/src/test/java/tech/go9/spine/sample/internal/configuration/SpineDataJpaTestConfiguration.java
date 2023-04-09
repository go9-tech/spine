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
package tech.go9.spine.sample.internal.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import tech.go9.spine.core.internal.configuration.SpineCoreConfiguration;
import tech.go9.spine.data.jpa.internal.configuration.SpineDataJpaConfiguration;
import tech.go9.spine.data.jpa.internal.repository.DefaultGenericRepository;
import tech.go9.spine.sample.api.domain.Hotel;
import tech.go9.spine.sample.api.repository.HotelRepository;

@Configuration
@ComponentScan("tech.go9.spine.sample")
@Import({ SpineCoreConfiguration.class, SpineDataJpaConfiguration.class })
@EntityScan(basePackageClasses = Hotel.class)
@EnableJpaRepositories(repositoryBaseClass = DefaultGenericRepository.class, basePackageClasses = HotelRepository.class)
public class SpineDataJpaTestConfiguration {

	/*
	 * @Bean
	 *
	 * @Profile("tenancy") public FilterEntity<City> cityTenancyFilter() { return new
	 * FilterEntity<City>() {
	 *
	 * @Override public Optional<Predicate> getRetrievePredicate() { return
	 * Optional.of(QCity.city.name.like("Bat%")); } }; }
	 *
	 * @Bean
	 *
	 * @Profile("tenancy") public FilterEntity<Hotel> hotelTenancyFilter() { return new
	 * FilterEntity<Hotel>() {
	 *
	 * @Override public Optional<Predicate> getRetrievePredicate() { return
	 * Optional.of(QHotel.hotel.name.like("The%")); } }; }
	 *
	 * @Bean
	 *
	 * @Profile("tenancy") public FilterEntity<Review> reviewTenancyFilter() { return new
	 * FilterEntity<Review>() {
	 *
	 * @Override public Optional<Predicate> getRetrievePredicate() { return
	 * Optional.of(QReview.review.title.like("Nice%")); } }; }
	 */

}
