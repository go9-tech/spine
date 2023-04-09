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
package tech.go9.spine.data.jpa.internal.usecase;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.support.Querydsl;

import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import tech.go9.spine.data.jpa.api.usecase.CreateJPQLQuery;
import tech.go9.spine.data.jpa.internal.util.EntityTenancyFilterUtils;

@AllArgsConstructor
public class DefaultCreateJPQLQuery<T> extends AbstractCreateQuery implements CreateJPQLQuery<T> {

	private final EntityManager entityManager;

	private final EntityPath<T> entityPath;

	private final Querydsl querydsl;

	@Override
	public JPQLQuery<T> execute(Optional<Pageable> pageable, Optional<Predicate> predicate,
			Optional<EntityGraph<T>> optionalEntityGraph, boolean forCount) {

		QueryMetadata queryMetadata = new DefaultQueryMetadata();
		queryMetadata.setProjection(this.entityPath);
		JPAQuery<T> jpaQuery = new JPAQuery<T>(this.entityManager, queryMetadata).from(this.entityPath);

		Optional<Predicate> tenancyPredicate = EntityTenancyFilterUtils.getRetrievePredicate(this.entityPath.getType());
		if (tenancyPredicate.isPresent()) {
			jpaQuery = jpaQuery.where(tenancyPredicate.get());
		}

		if (predicate.isPresent()) {
			jpaQuery = jpaQuery.where(predicate.get());
		}

		if (pageable.isPresent()) {
			jpaQuery = (JPAQuery<T>) this.querydsl.applyPagination(pageable.get(), jpaQuery);
		}

		if (!forCount && optionalEntityGraph.isPresent()) {
			super.createQuery(jpaQuery, this.entityPath.getType(), optionalEntityGraph.get().getAttributeNodes());
			jpaQuery.setHint(EntityGraphType.FETCH.getKey(), optionalEntityGraph.get());
		}

		return jpaQuery;
	}

}
