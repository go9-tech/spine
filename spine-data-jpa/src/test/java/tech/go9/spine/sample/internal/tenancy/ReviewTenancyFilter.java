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
package tech.go9.spine.sample.internal.tenancy;

import java.util.Optional;

import com.querydsl.core.types.Predicate;

import tech.go9.spine.data.jpa.api.tenancy.TenancyFilter;
import tech.go9.spine.sample.api.domain.QReview;
import tech.go9.spine.sample.api.domain.Review;

//@Component
// @Profile("tenancy")
public class ReviewTenancyFilter implements TenancyFilter<Review> {

	@Override
	public Optional<Predicate> getRetrievePredicate() {
		return Optional.of(QReview.review.title.like("Nice%"));
	}

}
