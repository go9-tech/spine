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
package tech.go9.spine.sample.api.controller;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/cities")
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface CityController {

	/*
	 * @PostMapping("/") public ControllerContext<Collection<City>> create(
	 *
	 * @RequestBody final Collection<City> payload);
	 *
	 * @PostMapping("/{id}") ControllerContext<City> create(
	 *
	 * @PathVariable("id") final Long id,
	 *
	 * @RequestBody final City payload);
	 *
	 * @GetMapping("/") ControllerContext<Page<City>> retrieve( final Pageable pageable,
	 * final Optional<Predicate> optionalPredicate, final Optional<ObjectNode> expand)
	 * throws NotAuthorizedException;
	 *
	 * @GetMapping("/{id}") ControllerContext<City> retrieve(
	 *
	 * @PathVariable("id") final Long id, final Optional<Predicate> optionalPredicate,
	 * final Optional<ObjectNode> expand);
	 *
	 * @PutMapping("/") ControllerContext<Collection<City>> update(
	 *
	 * @RequestBody final Collection<City> payload);
	 *
	 * @PutMapping("/{id}") ControllerContext<City> update(
	 *
	 * @PathVariable("id") final Long id,
	 *
	 * @RequestBody final City payload);
	 *
	 * @DeleteMapping("/") ControllerContext<Void> delete(
	 *
	 * @RequestBody final Collection<City> payload);
	 *
	 * @DeleteMapping("/{id}") ControllerContext<Void> delete(
	 *
	 * @PathVariable("id") final Long id,
	 *
	 * @RequestBody final City payload);
	 */

}
