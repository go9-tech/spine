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
package tech.go9.spine.data.jpa.internal.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.CrossTypeRevisionChangesReader;
import org.hibernate.envers.RevisionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import tech.go9.spine.core.api.exception.UnexpectedException;
import tech.go9.spine.data.jpa.api.repository.GenericRepository;
import tech.go9.spine.data.jpa.api.usecase.CreateJPQLQuery;
import tech.go9.spine.data.jpa.api.usecase.LoadEntity;
import tech.go9.spine.data.jpa.api.usecase.ParseEntityGraph;
import tech.go9.spine.data.jpa.internal.component.DefaultEntitySecurityManager;
import tech.go9.spine.data.jpa.internal.model.OperationType;
import tech.go9.spine.data.jpa.internal.usecase.DefaultCreateJPQLQuery;
import tech.go9.spine.data.jpa.internal.usecase.DefaultLoadEntity;
import tech.go9.spine.data.jpa.internal.usecase.DefaultParseEntityGraph;
import tech.go9.spine.data.jpa.internal.util.EntityUtils;
import tech.go9.spine.data.jpa.internal.util.QuerydslUtils;

@Slf4j
public class DefaultGenericRepository<T extends Serializable, I extends Serializable> extends SimpleJpaRepository<T, I>
		implements GenericRepository<T, I> {

	private final JpaEntityInformation<T, I> entityInformation;

	private final EntityManager entityManager;

	private final PathBuilder<T> pathBuilder;

	private final Querydsl querydsl;

	private final SimplePath<T> rootPath;

	private final SimplePath<?> idPath;

	private final CreateJPQLQuery<T> createJPQLQuery;

	private final ParseEntityGraph entityGraphParser;

	private final LoadEntity<T, I> entityLoader;

	public DefaultGenericRepository(JpaEntityInformation<T, I> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);
		this.entityInformation = entityInformation;
		this.entityManager = entityManager;
		EntityPathResolver entityPathResolver = SimpleEntityPathResolver.INSTANCE;
		EntityPath<T> entityPath = entityPathResolver.createPath(this.entityInformation.getJavaType());
		this.pathBuilder = new PathBuilder<>(entityPath.getType(), entityPath.getMetadata());
		this.querydsl = new Querydsl(this.entityManager, pathBuilder);
		this.rootPath = QuerydslUtils.createRootPath(this.entityInformation.getJavaType());
		this.idPath = QuerydslUtils.createIdPath(entityInformation, rootPath);
		this.createJPQLQuery = new DefaultCreateJPQLQuery<>(this.entityManager, entityPath, querydsl);
		this.entityGraphParser = new DefaultParseEntityGraph();
		this.entityLoader = new DefaultLoadEntity<>(this.entityManager, new DefaultEntitySecurityManager());
	}

	@Override
	@Transactional
	public Stream<T> saveAll(Stream<T> entities) {
		Assert.notNull(entities, "Entities must not be null!");
		return entities.map(this::saveOne);
	}

	@Override
	@Transactional
	public Stream<T> saveAll(Stream<T> entities, boolean loadEntities) {
		Assert.notNull(entities, "Entities must not be null!");
		return entities.map(entity -> this.saveOne(entity, loadEntities));
	}

	@Override
	@Transactional
	public T saveOne(T entity) {
		Assert.notNull(entity, "Entity must not be null!");
		return this.saveOne(entity, true);
	}

	@Override
	@Transactional
	public T saveOne(T entity, boolean loadEnty) {
		Assert.notNull(entity, "Entity must not be null!");
		Assert.notNull(loadEnty, "loadEnty must not be null!");
		if (!loadEnty) {
			return this.saveByHibernate(entity);
		}
		else {
			return this.entityLoader.execute(entity, OperationType.SAVE)
					.map(loadedEntity -> this.saveByHibernate(loadedEntity)).orElseThrow(() -> {
						Object idValue = this.getIdValue(entity);
						idValue = idValue == null ? "NEW" : idValue;
						return new UnexpectedException("Error saving %s[%s], check if entity exists", entity.getClass(),
								idValue);
					});
		}
	}

	@Override
	public Stream<T> findAll(final Predicate predicate) {
		Assert.notNull(predicate, "Predicate must not be null!");
		return this.findAll(Optional.of(predicate), Optional.empty());
	}

	@Override
	public Stream<T> findAll(final String expand) {
		Assert.notNull(expand, "GraphExpression must not be null!");
		return this.findAll(Optional.empty(), Optional.of(expand));
	}

	@Override
	public Stream<T> findAll(final Predicate predicate, final String expand) {
		Assert.notNull(predicate, "Predicate must not be null!");
		Assert.notNull(expand, "GraphExpression must not be null!");
		return this.findAll(Optional.of(predicate), Optional.of(expand));
	}

	@Override
	public Stream<T> findAll(Optional<Predicate> predicate, Optional<String> expand) {
		Assert.notNull(predicate, "Predicate must not be null!");
		Assert.notNull(expand, "GraphExpression must not be null!");
		Optional<EntityGraph<T>> entityGraph = parseEntityGraph(expand);
		JPQLQuery<T> jpqlQuery = this.createJPQLQuery.execute(Optional.empty(), predicate, entityGraph, false);
		return jpqlQuery.stream();
	}

	@Override
	public Stream<T> findAll(JPQLQuery<T> jpqlQuery) {
		return jpqlQuery.stream();
	}

	@Override
	public Page<T> findAll(final Pageable pageable) {
		Assert.notNull(pageable, "Pageable must not be null!");
		return this.findAll(pageable, Optional.empty(), Optional.empty());
	}

	@Override
	public Page<T> findAll(final Pageable pageable, final Predicate predicate) {
		Assert.notNull(pageable, "Pageable must not be null!");
		Assert.notNull(predicate, "Predicate must not be null!");
		return this.findAll(pageable, Optional.of(predicate), Optional.empty());
	}

	@Override
	public Page<T> findAll(final Pageable pageable, final String expand) {
		Assert.notNull(pageable, "Pageable must not be null!");
		Assert.notNull(expand, "GraphExpression must not be null!");
		return this.findAll(pageable, Optional.empty(), Optional.of(expand));
	}

	@Override
	public Page<T> findAll(final Pageable pageable, final Predicate predicate, final String expand) {
		Assert.notNull(pageable, "Pageable must not be null!");
		Assert.notNull(predicate, "Predicate must not be null!");
		Assert.notNull(expand, "GraphExpression must not be null!");
		return this.findAll(pageable, Optional.of(predicate), Optional.of(expand));
	}

	@Override
	public Page<T> findAll(Pageable pageable, final Optional<Predicate> predicate, final Optional<String> expand) {
		Optional<EntityGraph<T>> entityGraph = parseEntityGraph(expand);
		JPQLQuery<T> countJpqlQuery = null;
		JPQLQuery<T> mainJpqlQuery = this.createJPQLQuery.execute(Optional.of(pageable), predicate, entityGraph, false);
		LongSupplier counter = null;

		// and expand one collection - melhorar o if
		if (expand.isPresent()) {

			log.warn("Avoid use expand withou a real necesary predicate, this could present performance issues");

			countJpqlQuery = this.createJPQLQuery.execute(Optional.empty(), predicate, entityGraph, true);
			List<?> queryResults = ((JPAQuery<T>) countJpqlQuery).select(this.idPath).distinct().fetch();
			counter = () -> queryResults.size();

			// transformar este in em subquery?
			mainJpqlQuery.where(this.pathBuilder.get(this.idPath).in((Collection) queryResults));

		}
		else {

			countJpqlQuery = this.createJPQLQuery.execute(Optional.of(pageable), predicate, entityGraph, true);
			counter = countJpqlQuery::fetchCount;
		}

		return PageableExecutionUtils.getPage(mainJpqlQuery.fetch(), pageable, counter);
	}

	@Override
	public Page<T> findAll(Pageable pageable, JPQLQuery<T> query) {
		return PageableExecutionUtils.getPage(query.fetch(), pageable, query::fetchCount);
	}

	@Override
	public Optional<T> findById(I id) {
		return this.findById(id, Optional.empty(), Optional.empty());
	}

	@Override
	public Optional<T> findById(final I id, final Predicate predicate) {
		Assert.notNull(id, "Id must not be null!");
		Assert.notNull(predicate, "Predicate must not be null!");
		return this.findById(id, Optional.of(predicate), Optional.empty());
	}

	@Override
	public Optional<T> findById(final I id, final String expand) {
		Assert.notNull(id, "Id must not be null!");
		Assert.notNull(expand, "GraphExpression must not be null!");
		return this.findById(id, Optional.empty(), Optional.of(expand));
	}

	@Override
	public Optional<T> findById(final I id, final Predicate predicate, final String expand) {
		Assert.notNull(id, "Id must not be null!");
		Assert.notNull(predicate, "Predicate must not be null!");
		Assert.notNull(expand, "GraphExpression must not be null!");
		return this.findById(id, Optional.of(predicate), Optional.of(expand));
	}

	@Override
	public Optional<T> findById(final I id, final Optional<Predicate> predicate, final Optional<String> expand) {
		Predicate mergedPredicate = mergeToPredicate(id, predicate);
		Optional<EntityGraph<T>> entityGraph = parseEntityGraph(expand);
		JPQLQuery<T> jpqlQuery = this.createJPQLQuery.execute(Optional.empty(), Optional.of(mergedPredicate),
				entityGraph, false);
		return Optional.ofNullable(jpqlQuery.fetchOne());
	}

	@Override
	public Optional<T> findOne(final Predicate predicate) {
		Assert.notNull(predicate, "Predicate must not be null!");
		return this.findOne(Optional.of(predicate), Optional.empty());
	}

	@Override
	public Optional<T> findOne(final Predicate predicate, final String expand) {
		Assert.notNull(predicate, "Predicate must not be null!");
		Assert.notNull(expand, "GraphExpression must not be null!");
		return this.findOne(Optional.of(predicate), Optional.of(expand));
	}

	@Override
	public Optional<T> findOne(Optional<Predicate> predicate, Optional<String> expand) {
		Optional<EntityGraph<T>> entityGraph = parseEntityGraph(expand);
		JPQLQuery<T> jpqlQuery = this.createJPQLQuery.execute(Optional.empty(), predicate, entityGraph, false);
		return Optional.ofNullable(jpqlQuery.fetchOne());
	}

	@Override
	public Optional<T> findOne(JPQLQuery<T> query) {
		return Optional.ofNullable(query.fetchOne());
	}

	@Override
	public JPQLQuery<T> createQuery(Optional<Pageable> pageable, Optional<String> expand) {
		Optional<EntityGraph<T>> entityGraph = parseEntityGraph(expand);
		return this.createJPQLQuery.execute(pageable, Optional.empty(), entityGraph, false);
	}

	@Override
	@Transactional
	public void deleteAll(final Stream<T> entities) {
		Assert.notNull(entities, "Entities must not be null!");
		entities.peek(this::deleteOne).toArray();
	}

	@Override
	@Transactional
	public void deleteOne(T entity) {
		Assert.notNull(entity, "Entity must not be null!");
		if (this.entityInformation.isNew(entity)) {
			return;
		}
		this.entityLoader.execute(entity, OperationType.DELETE).ifPresent(loadedEntity -> {
			this.entityManager.remove(loadedEntity);
		});
	}

	@Override
	@Transactional
	public void deleteById(I id) {
		Assert.notNull(id, "Id must not be null!");
		this.findById(id).ifPresent(loadedEntity -> {
			this.entityManager.remove(loadedEntity);
		});
	}

	@Override
	public void flush() {
		this.entityManager.flush();
	}

	@Override
	public Map<Number, T> findAllRevisions(I id) {
		Assert.notNull(id, "Id must not be null!");
		return this.findById(id).map(entity -> {
			AuditReader auditReader = AuditReaderFactory.get(this.entityManager);
			Map<Number, T> revisions = new LinkedHashMap<>();
			auditReader.getRevisions(this.entityInformation.getJavaType(), id).forEach(revision -> revisions
					.put(revision, auditReader.find(this.entityInformation.getJavaType(), id, revision)));
			return revisions;
		}).orElseGet(HashMap::new);
	}

	@Override
	// TODO como aplicar segurança neste caso? somente para "administradores"?
	public Map<RevisionType, List<Object>> findEntitiesGroupByRevisionType(Number revision) {
		Assert.notNull(revision, "revision must not be null!");
		AuditReader auditReader = AuditReaderFactory.get(this.entityManager);
		CrossTypeRevisionChangesReader crossTypeRevisionChangesReader = auditReader.getCrossTypeRevisionChangesReader();
		return crossTypeRevisionChangesReader.findEntitiesGroupByRevisionType(revision);
	}

	private Predicate mergeToPredicate(I id, Optional<Predicate> predicate) {
		Predicate mergedPredicate = Expressions.predicate(Ops.EQ, this.idPath, Expressions.constant(id));
		if (predicate.isPresent()) {
			mergedPredicate = ExpressionUtils.and(mergedPredicate, predicate.get());
		}
		return mergedPredicate;
	}

	private Optional<EntityGraph<T>> parseEntityGraph(Optional<String> expand) {
		return expand
				.map(g -> this.entityGraphParser.execute(this.entityManager, this.entityInformation.getJavaType(), g));
	}

	private T saveByJpa(T entity) {
		if (this.entityInformation.isNew(entity)) {
			this.entityManager.persist(entity);
			return entity;
		}
		else {
			return this.entityManager.merge(entity);
		}
	}

	private T saveByHibernate(T entity) {
		if (this.entityInformation.isNew(entity)) {
			this.entityManager.unwrap(Session.class).persist(entity);
		}
		else {
			this.entityManager.unwrap(Session.class).merge(entity);
		}
		return entity;
	}

	private <N> Object getIdValue(N entity) {
		try {
			return EntityUtils.getIdValue(entity);
		}
		catch (Exception exception) {
			throw new UnexpectedException("Unable to get id of entity %s", exception, entity.getClass().getName());
		}
	}

}
