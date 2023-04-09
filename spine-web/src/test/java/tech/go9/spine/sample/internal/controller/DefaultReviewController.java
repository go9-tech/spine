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
package tech.go9.spine.sample.internal.controller;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import tech.go9.spine.sample.api.controller.ReviewController;
import tech.go9.spine.sample.api.domain.Review;
import tech.go9.spine.sample.api.usecase.SaveReviews;

@AllArgsConstructor
@RestController
@ConditionalOnSingleCandidate(ReviewController.class)
public class DefaultReviewController implements ReviewController {

	private final SaveReviews saveReviews;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Review> create(List<Review> reviews) {
		return this.saveReviews.execute(reviews);
	}

}
