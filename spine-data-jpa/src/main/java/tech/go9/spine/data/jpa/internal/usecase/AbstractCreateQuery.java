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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.graph.internal.AbstractGraph;
import org.hibernate.graph.internal.AttributeNodeImpl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.jpa.JPQLQuery;

import jakarta.persistence.AttributeNode;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Subgraph;
import tech.go9.spine.core.api.exception.UnexpectedException;
import tech.go9.spine.core.api.util.ReflectionUtils;
import tech.go9.spine.data.jpa.internal.util.EntityTenancyFilterUtils;
import tech.go9.spine.data.jpa.internal.util.QuerydslUtils;

public class AbstractCreateQuery {

	protected void createQuery(final JPQLQuery<?> jpqlQuery, final Class<?> clazz,
			final List<AttributeNode<?>> attributeNodes) {
		attributeNodes.forEach((AttributeNode<?> attributeNode) -> {

			AttributeNodeImpl<?> attributeNodeImpl = (AttributeNodeImpl<?>) attributeNode;
			String attributeName = attributeNodeImpl.getAttributeName();
			Class<?> attributeType = attributeNodeImpl.getAttributeDescriptor().getJavaType();
			if (Collection.class.isAssignableFrom(attributeType)) {
				Field field = ReflectionUtils.findField(clazz, attributeName);
				attributeType = ReflectionUtils.getCollectionType(field);
			}

			if (attributeNode.getSubgraphs().isEmpty()) {
				join(jpqlQuery, clazz, attributeName, attributeType);
			}
			else {

				for (Subgraph<?> subgraph : attributeNode.getSubgraphs().values()) {
					AbstractGraph<?> abstractGraph = (AbstractGraph<?>) subgraph;
					attributeType = abstractGraph.getGraphedType().getJavaType();
					JPQLQuery<?> joinnedjpqlQuery = join(jpqlQuery, clazz, attributeName, attributeType);
					createQuery(joinnedjpqlQuery, attributeType, subgraph.getAttributeNodes());
				}
			}
		});
	}

	private <A, B> JPQLQuery<?> join(JPQLQuery<?> jpqlQuery, Class<A> clazz, String attributeName,
			Class<B> attributeType) {

		JPQLQuery<?> joinnedjpqlQuery = null;
		Field field = ReflectionUtils.findField(clazz, attributeName);
		// TODO ver a cardinalidade para determinar left join
		if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
			joinnedjpqlQuery = jpqlQuery.leftJoin(this.createlEntityPath(clazz, attributeName, attributeType),
					createPath(clazz, attributeName, attributeType)).fetchJoin();
		}
		else if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
			joinnedjpqlQuery = jpqlQuery.leftJoin(createCollectionExpression(clazz, attributeName, attributeType),
					createPath(clazz, attributeName, attributeType)).fetchJoin();
		}
		else {
			throw new UnexpectedException("TBD");
		}
		Optional<Predicate> optionalPredicate = EntityTenancyFilterUtils.getRetrievePredicate(attributeType);
		if (optionalPredicate.isPresent()) {
			jpqlQuery.where(optionalPredicate.get());
		}
		return joinnedjpqlQuery;
	}

	private <A, B> CollectionExpression<?, B> createCollectionExpression(Class<A> clazz, String attributeName,
			Class<B> attributeType) {
		SimplePath<A> rootPath = QuerydslUtils.createRootPath(clazz);
		PathBuilder<A> pathBuilder = new PathBuilder<>(clazz, rootPath.getMetadata().getName());
		Field field = ReflectionUtils.findField(clazz, attributeName);
		CollectionExpression<?, B> collectionExpression = null;
		if (field.getType().isAssignableFrom(Set.class)) {
			collectionExpression = pathBuilder.getSet(attributeName, attributeType);
		}
		else if (field.getType().isAssignableFrom(List.class)) {
			collectionExpression = pathBuilder.getList(attributeName, attributeType);
		}
		else if (field.getType().isAssignableFrom(Collection.class)) {
			collectionExpression = pathBuilder.getCollection(attributeName, attributeType);
		}
		return collectionExpression;
	}

	private <A, B> EntityPath<B> createlEntityPath(Class<A> clazz, String attributeName, Class<B> attributeType) {
		SimplePath<A> rootPath = QuerydslUtils.createRootPath(clazz);
		PathBuilder<A> pathBuilder = new PathBuilder<>(clazz, rootPath.getMetadata().getName());
		return (EntityPath<B>) pathBuilder.get(attributeName, attributeType);
	}

	private <A, B> Path<B> createPath(Class<A> clazz, String attributeName, Class<B> attributeType) {
		return SimpleEntityPathResolver.INSTANCE.createPath(attributeType);
	}

}
