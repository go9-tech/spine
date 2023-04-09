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
package tech.go9.spine.data.jpa.api.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.hibernate.envers.RevisionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.NoRepositoryBean;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;

@NoRepositoryBean
public interface GenericRepository<T extends Serializable, I extends Serializable>
		extends JpaRepositoryImplementation<T, I> {

	Stream<T> saveAll(Stream<T> entities);

	Stream<T> saveAll(Stream<T> entities, boolean loadEntities);

	T saveOne(T entity);

	T saveOne(T entity, boolean loadEnty);

	// Stream<T> findAll();

	Stream<T> findAll(Predicate predicate);

	Stream<T> findAll(String expand);

	Stream<T> findAll(Predicate predicate, String expand);

	Stream<T> findAll(Optional<Predicate> predicate, Optional<String> expand);

	Stream<T> findAll(JPQLQuery<T> query);

	Page<T> findAll(Pageable pageable);

	Page<T> findAll(Pageable pageable, Predicate predicate);

	Page<T> findAll(Pageable pageable, String expand);

	Page<T> findAll(Pageable pageable, Predicate predicate, String expand);

	Page<T> findAll(Pageable pageable, Optional<Predicate> predicate, Optional<String> expand);

	Page<T> findAll(Pageable pageable, JPQLQuery<T> query);

	Optional<T> findById(I id);

	Optional<T> findById(I id, Predicate predicate);

	Optional<T> findById(I id, String expand);

	Optional<T> findById(I id, Predicate predicate, String expand);

	Optional<T> findById(I id, Optional<Predicate> predicate, Optional<String> expand);

	// Optional<T> findOne();

	Optional<T> findOne(Predicate predicate);

	Optional<T> findOne(Predicate predicate, String expand);

	Optional<T> findOne(Optional<Predicate> predicate, Optional<String> expand);

	Optional<T> findOne(JPQLQuery<T> query);

	JPQLQuery<T> createQuery(Optional<Pageable> pageable, Optional<String> expand);

	void deleteAll(Stream<T> entities);

	void deleteOne(T entity);

	void deleteById(I id);

	public void flush();

	Map<Number, T> findAllRevisions(I id);

	Map<RevisionType, List<Object>> findEntitiesGroupByRevisionType(Number revision);

}
