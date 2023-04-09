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

import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.web.bind.annotation.RestController;

import tech.go9.spine.demo.api.controller.CityController;

@RestController
@ConditionalOnSingleCandidate(CityController.class)
public class DefaultCityController implements CityController {

	/*
	 * @Autowired private CityRepository service;
	 *
	 * @Override public ControllerContext<Collection<City>> create(Collection<City>
	 * payload) { ControllerContext<Collection<City>> controllerContext = new
	 * ControllerContext<>(); controllerContext.setContent(this.service.saveAl(payload));
	 * return controllerContext; }
	 *
	 * @Override public ControllerContext<City> create(Long id, City payload) {
	 * ControllerContext<City> controllerContext = new ControllerContext<>();
	 * controllerContext.setContent(this.service.create(payload)); return
	 * controllerContext; }
	 *
	 * @Override public ControllerContext<Page<City>> retrieve(Pageable pageable, final
	 * Optional<Predicate> optionalPredicate, final Optional<ObjectNode>
	 * optionalObjectGraph) { ControllerContext<Page<City>> controllerContext = new
	 * ControllerContext<>(); controllerContext.setContent(this.service.retrieve(pageable,
	 * optionalPredicate, optionalObjectGraph)); return controllerContext; }
	 *
	 * @Override public ControllerContext<City> retrieve(Long id, final
	 * Optional<Predicate> optionalPredicate, final Optional<ObjectNode>
	 * optionalObjectGraph) { ControllerContext<City> controllerContext = new
	 * ControllerContext<>(); controllerContext.setContent(this.service.retrieve(id,
	 * optionalPredicate, optionalObjectGraph)); return controllerContext; }
	 *
	 * @Override public ControllerContext<Collection<City>> update(Collection<City>
	 * payload) { ControllerContext<Collection<City>> controllerContext = new
	 * ControllerContext<>(); controllerContext.setContent(this.service.update(payload));
	 * return controllerContext; }
	 *
	 * @Override public ControllerContext<City> update(Long id, City payload) {
	 * ControllerContext<City> controllerContext = new ControllerContext<>();
	 * controllerContext.setContent(this.service.update(payload)); return
	 * controllerContext; }
	 *
	 * @Override public ControllerContext<Void> delete(Collection<City> payload) {
	 * ControllerContext<Void> controllerContext = new ControllerContext<>();
	 * this.service.delete(payload); controllerContext.setContent((Void) null); return
	 * controllerContext; }
	 *
	 * @Override public ControllerContext<Void> delete(Long id, City payload) {
	 * ControllerContext<Void> controllerContext = new ControllerContext<>();
	 * this.service.delete(payload); controllerContext.setContent((Void) null); return
	 * controllerContext; }
	 */

}
