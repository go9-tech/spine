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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.PersistentAttributeInterceptable;
import org.hibernate.engine.spi.SelfDirtinessTracker;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.go9.spine.core.api.exception.UnexpectedException;
import tech.go9.spine.core.api.util.ReflectionUtils;
import tech.go9.spine.data.jpa.api.usecase.AuthorizeEntity;
import tech.go9.spine.data.jpa.api.usecase.LoadEntity;
import tech.go9.spine.data.jpa.internal.model.OperationType;
import tech.go9.spine.data.jpa.internal.util.EntityUtils;

@Slf4j
@AllArgsConstructor
public class DefaultLoadEntity<T, I extends Serializable> implements LoadEntity<T, I> {

	private static final Collection<CascadeType> MERGE_CASCADES = Arrays.asList(CascadeType.ALL, CascadeType.PERSIST);

	private final EntityManager entityManager;

	private final AuthorizeEntity entitySecurityManager;

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Optional<T> execute(Class<T> clazz, I id, OperationType operationType) {
		Assert.notNull(clazz, "clazz must not be null!");
		Assert.notNull(id, "id operationType must not be null");
		Assert.notNull(id, "operationType operationType must not be null");
		return this.loadEntity(clazz, id).map(attachedEntity -> {
			this.entitySecurityManager.authorize(attachedEntity, operationType);
			return attachedEntity;
		});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Optional<T> execute(final T entity, final OperationType operationType) {
		Assert.notNull(entity, "entity must not be null!");
		Assert.notNull(operationType, "operationType operationType must not be null");
		Map<Object, Object> historyMap = new HashMap<>();
		return loadEntity(entity, historyMap, true).map(attachedEntity -> {
			this.deepLoad(entity, attachedEntity, operationType, historyMap, true);
			return attachedEntity;
		});
	}

	private void deepLoad(Object detachedEntity, Object attachedEntity, OperationType operationType,
			Map<Object, Object> historyMap, boolean merge) {

		this.entitySecurityManager.authorize(attachedEntity, operationType);

		Object historyAttachedEntity = historyMap.get(detachedEntity);
		if (historyAttachedEntity != null) {
			return;
		}
		else {
			historyMap.put(detachedEntity, attachedEntity);
		}

		if (merge && operationType.equals(OperationType.SAVE)) {
			mergeAttributes(detachedEntity, attachedEntity);
		}

		List<Field> fields = EntityUtils.getOneToManyFields(detachedEntity);
		for (Field field : fields) {
			boolean mergeChild = getMergeChild(operationType, field.getAnnotation(OneToMany.class).cascade(), merge);
			this.deepLoadToMany(detachedEntity, attachedEntity, field, operationType, historyMap, mergeChild);
		}

		fields = EntityUtils.getManyToManyFields(detachedEntity);
		for (Field field : fields) {
			boolean mergeChild = getMergeChild(operationType, field.getAnnotation(ManyToMany.class).cascade(), merge);
			this.deepLoadToMany(detachedEntity, attachedEntity, field, operationType, historyMap, mergeChild);
		}

		fields = EntityUtils.getOneToOneFields(detachedEntity);
		for (Field field : fields) {
			boolean mergeChild = getMergeChild(operationType, field.getAnnotation(OneToOne.class).cascade(), merge);
			this.deepLoadToOne(detachedEntity, attachedEntity, field, operationType, historyMap, mergeChild);
		}

		fields = EntityUtils.getManyToOneFields(detachedEntity);
		for (Field field : fields) {
			boolean mergeChild = getMergeChild(operationType, field.getAnnotation(ManyToOne.class).cascade(), merge);
			this.deepLoadToOne(detachedEntity, attachedEntity, field, operationType, historyMap, mergeChild);
		}
	}

	/*
	 * Merge simple attributes
	 */

	private <N> void mergeAttributes(final N detachedEntity, final N attachedEntity) {

		Field idField = EntityUtils.getIdField(detachedEntity.getClass());
		Object detachedEntityId = getFieldValue(detachedEntity, idField, true);
		setFieldValue(attachedEntity, idField, detachedEntityId, true);

		this.listFieldsToMerge(detachedEntity).forEach(field -> {
			this.entitySecurityManager.authorize(attachedEntity, field);
			if (log.isDebugEnabled()) {
				this.logAttributeMerge(detachedEntity, attachedEntity, detachedEntityId, field);
			}
			Object value = this.getFieldValue(detachedEntity, field);
			this.setFieldValue(attachedEntity, field, value);
		});
	}

	private Collection<Field> listFieldsToMerge(Object entity) {
		if (SelfDirtinessTracker.class.isAssignableFrom(entity.getClass())) {
			return EntityUtils.getDirtyFields((SelfDirtinessTracker) entity);
		}
		else {
			return EntityUtils.getNonRelationalFields(entity);
		}
	}

	private boolean getMergeChild(OperationType operationType, CascadeType[] cascadeTypes, boolean merge) {
		if (!merge) {
			return false;
		}
		Collection<CascadeType> cascades = this.getCascades(operationType);
		return CollectionUtils.containsAny(Arrays.asList(cascadeTypes), cascades);
	}

	private Collection<CascadeType> getCascades(OperationType operationType) {
		if (operationType.equals(OperationType.SAVE) || operationType.equals(OperationType.DELETE)) {
			return MERGE_CASCADES;
		}
		else {
			return Collections.emptyList();
		}
	}

	/*
	 * Merge to many attributes
	 */

	private void deepLoadToMany(Object parentDetachedEntity, Object parentAttachedEntity, Field field,
			OperationType operationType, Map<Object, Object> historyMap, boolean merge) {

		Collection<Object> detachedCollection = null;
		Optional<Object> optional = this.getDetachedObject(parentDetachedEntity, field);
		if (optional == null) {
			return;
		}
		else if (optional.isPresent()) {
			detachedCollection = this.castCollection(optional.get());
		}

		Object attachedFieldValue = getFieldValue(parentAttachedEntity, field);
		Collection<Object> attachedCollection = null;
		if (attachedFieldValue != null) {
			attachedCollection = this.castCollection(attachedFieldValue);
		}

		List<Pair<Optional<Object>, Optional<Object>>> pairs = this.listEntityPairs(detachedCollection,
				attachedCollection);
		for (Pair<Optional<Object>, Optional<Object>> pair : pairs) {
			Object detachedEntity = pair.getFirst().orElse(null);
			Object attachedEntity = pair.getSecond().orElse(null);

			if (detachedEntity != null && attachedEntity == null) {

				if (merge) {

					if (attachedCollection == null) {
						attachedCollection = this.instantiateCollection(field);
						setFieldValue(parentAttachedEntity, field, attachedCollection);
					}

					attachedEntity = historyMap.get(detachedEntity);
					if (attachedEntity == null) {
						Object id = this.getIdValue(detachedEntity);
						if (id == null) {
							Class<?> clazz = ReflectionUtils.getClass(detachedEntity);
							attachedEntity = ReflectionUtils.newInstance(clazz);
						}
						else {
							Optional<Object> optionalAttachedEntity = this.loadEntity(detachedEntity, historyMap,
									false);
							if (optionalAttachedEntity.isPresent()) {
								attachedEntity = optionalAttachedEntity.get();
							}
						}
					}

					attachedCollection.add(attachedEntity);
					EntityUtils.setMappedByFields(parentAttachedEntity, field, attachedEntity, false);
					this.deepLoad(detachedEntity, attachedEntity, operationType, historyMap, merge);

				}
				else {

					Optional<Object> optionalAttachedEntity = this.loadEntity(detachedEntity, historyMap, false);
					if (optionalAttachedEntity.isPresent()) {

						if (attachedCollection == null) {
							attachedCollection = this.instantiateCollection(field);
							setFieldValue(parentAttachedEntity, field, attachedCollection);
						}
						attachedEntity = optionalAttachedEntity.get();
						attachedCollection.add(attachedEntity);
						EntityUtils.setMappedByFields(parentAttachedEntity, field, attachedEntity, false);
						this.deepLoad(detachedEntity, attachedEntity, operationType, historyMap, merge);

					}
					else {

						EntityUtils.setMappedByFields(parentDetachedEntity, field, detachedEntity, true);
						detachedCollection.remove(detachedEntity);
					}
				}

			}
			else if (detachedEntity == null && attachedEntity != null) {

				if (merge) {
					if (detachedCollection != null) {
						attachedCollection.remove(attachedEntity);
						EntityUtils.setMappedByFields(parentAttachedEntity, field, attachedEntity, true);
						this.deleteEntity(attachedEntity);
					}
				}
				else {
					// do nothing
				}

			}
			else if (detachedEntity != null && attachedEntity != null) {

				this.deepLoad(detachedEntity, attachedEntity, operationType, historyMap, merge);
			}
		}
	}

	private List<Pair<Optional<Object>, Optional<Object>>> listEntityPairs(Collection<Object> detachedCollection,
			Collection<Object> attachedCollection) {

		Set<Object> history = new LinkedHashSet<>();
		List<Pair<Optional<Object>, Optional<Object>>> pairs = new ArrayList<>();

		if (attachedCollection != null && detachedCollection == null) {

			attachedCollection
					.forEach(attachedEntity -> pairs.add(Pair.of(Optional.empty(), Optional.of(attachedEntity))));

		}
		else if (attachedCollection == null && detachedCollection != null) {

			detachedCollection
					.forEach(detachedEntity -> pairs.add(Pair.of(Optional.of(detachedEntity), Optional.empty())));

		}
		else if (attachedCollection != null && detachedCollection != null) {

			for (Object attachedEntity : attachedCollection) {
				this.findEntity(detachedCollection, attachedEntity).ifPresentOrElse(detachedEntity -> {
					Object idValue = this.getIdValue(detachedEntity);
					if (idValue != null) {
						history.add(idValue);
						pairs.add(Pair.of(Optional.of(detachedEntity), Optional.of(attachedEntity)));
					}
				}, () -> {
					Object idValue = this.getIdValue(attachedEntity);
					if (idValue != null) {
						history.add(idValue);
						pairs.add(Pair.of(Optional.empty(), Optional.of(attachedEntity)));
					}
				});
			}

			for (Object detachedEntity : detachedCollection) {
				this.findEntity(attachedCollection, detachedEntity).ifPresentOrElse(attachedEntity -> {
					Object idValue = this.getIdValue(attachedEntity);
					if (idValue != null && !history.contains(idValue)) {
						history.add(idValue);
					}
					pairs.add(Pair.of(Optional.of(detachedEntity), Optional.of(attachedEntity)));
				}, () -> {
					Object idValue = this.getIdValue(detachedEntity);
					if (idValue != null && !history.contains(idValue)) {
						history.add(idValue);
					}
					pairs.add(Pair.of(Optional.of(detachedEntity), Optional.empty()));
				});
			}
		}

		return pairs;
	}

	/*
	 * Merge to one attribute
	 */

	private void deepLoadToOne(Object parentDetachedEntity, Object parentAttachedEntity, Field field,
			OperationType operationType, Map<Object, Object> historyMap, boolean merge) {

		Object detachedEntity = null;
		Optional<Object> optional = this.getDetachedObject(parentDetachedEntity, field);
		if (optional != null) {
			detachedEntity = optional.orElse(null);
		}

		Object attachedEntity = this.getFieldValue(parentAttachedEntity, field);
		if (attachedEntity == null && detachedEntity != null) {
			attachedEntity = this.loadEntity(detachedEntity, historyMap, false).orElse(null);
		}

		if (detachedEntity != null && attachedEntity == null) {

			if (merge) {

				attachedEntity = historyMap.get(detachedEntity);
				if (attachedEntity == null) {
					Class<?> clazz = ReflectionUtils.getClass(detachedEntity);
					attachedEntity = ReflectionUtils.newInstance(clazz);
				}
				this.setFieldValue(parentAttachedEntity, field, attachedEntity);
				EntityUtils.setMappedByFields(parentAttachedEntity, field, attachedEntity, false);
				this.deepLoad(detachedEntity, attachedEntity, operationType, historyMap, merge);

			}
			else {
				// do nothing
			}

		}
		else if (detachedEntity == null && attachedEntity != null) {

			if (merge) {
				// delete attached entity
				this.setFieldValue(parentAttachedEntity, field, null);
				EntityUtils.setMappedByFields(parentAttachedEntity, field, attachedEntity, true);
				this.deleteEntity(attachedEntity);
			}
			else {
				// do nothing
			}

		}
		else if (detachedEntity != null && attachedEntity != null) {

			this.setFieldValue(parentAttachedEntity, field, attachedEntity);
			EntityUtils.setMappedByFields(parentAttachedEntity, field, attachedEntity, false);
			this.deepLoad(detachedEntity, attachedEntity, operationType, historyMap, merge);
		}
	}

	private Optional<Object> getDetachedObject(Object parentDetachedEntity, Field field) {

		if (!field.canAccess(parentDetachedEntity)) {
			field.setAccessible(true);
		}

		Optional<Object> detachedFieldValue = null;
		try {

			Object filedValue = field.get(parentDetachedEntity);
			if ((filedValue instanceof HibernateProxy || filedValue instanceof PersistentAttributeInterceptable
					|| filedValue instanceof PersistentCollection)) {

				if (Hibernate.isInitialized(filedValue)) {
					detachedFieldValue = Optional.ofNullable(this.getFieldValue(parentDetachedEntity, field));
				}
				else if (log.isDebugEnabled()) {
					Object detachedEntityId = this.getIdValue(parentDetachedEntity);
					log.debug(String.format("Ignoring field %s[%s].%s, not initialized",
							parentDetachedEntity.getClass().getCanonicalName(),
							detachedEntityId == null ? "NEW" : detachedEntityId, field.getName()));
				}

			}
			else {
				detachedFieldValue = Optional.ofNullable(filedValue);
			}

		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			throw new UnexpectedException("Error getting field %s.%s value", field.getName(),
					parentDetachedEntity.getClass().getCanonicalName());
		}

		return detachedFieldValue;
	}

	private <N> Collection<N> instantiateCollection(Field field) {
		if (List.class.isAssignableFrom(field.getType())) {
			return new ArrayList<>();
		}
		else if (Set.class.isAssignableFrom(field.getType())) {
			return new LinkedHashSet<>();
		}
		else {
			throw new UnexpectedException("Unsupported collection type: %s", field.getClass().getCanonicalName());
		}
	}

	private <N> Optional<N> loadEntity(N detachedEntity, Map<Object, Object> historyMap, boolean instantiate) {
		N historyEntity = (N) historyMap.get(detachedEntity);
		if (historyEntity != null) {
			return Optional.of(historyEntity);
		}
		else if (this.entityManager.contains(detachedEntity)) {
			return Optional.of(detachedEntity);
		}
		else {
			Class<N> clazz = ReflectionUtils.getClass(detachedEntity);
			Object id = this.getIdValue(detachedEntity);
			if (id == null && instantiate) {
				log.debug("CREATE {}", clazz.getName());
				return Optional.of(ReflectionUtils.newInstance(clazz));
			}
			else if (id == null) {
				return Optional.empty();
			}
			else {
				return this.loadEntity(clazz, id);
			}
		}
	}

	private <N> Optional<N> loadEntity(Class<N> clazz, Object id) {
		log.debug("LOAD {}[{}]", clazz.getName(), id);
		Optional<N> entity = Optional.ofNullable(this.entityManager.find(clazz, id));
		if (log.isDebugEnabled() && entity.isEmpty()) {
			log.debug("Entity {}[{}] not found", clazz.getName(), id);
		}
		return entity;
	}

	private void deleteEntity(Object entity) {
		this.entityManager.remove(entity);
		log.debug(String.format("DELETE %s[%s]", entity.getClass().getName(), entity));
	}

	private <N> Optional<N> findEntity(Collection<N> collection, N entity) {
		try {
			Optional<N> result = EntityUtils.findEntity(collection, entity);
			if (log.isDebugEnabled() && result.isEmpty()) {
				log.warn("Entity {}[{}] not found in collection {}", entity.getClass(), this.getIdValue(entity),
						collection.toString());
			}
			return result;
		}
		catch (Exception exception) {
			throw new UnexpectedException("Unable to get entity %s[%s] in collection %s", exception, entity.getClass(),
					this.getIdValue(entity), collection.toString());
		}
	}

	private Object getIdValue(Object entity) {
		try {
			return EntityUtils.getIdValue(entity);
		}
		catch (Exception exception) {
			throw new UnexpectedException("Unable to get %s entity id", exception, entity.getClass().getName());
		}
	}

	private Object getFieldValue(Object object, Field field) {
		return this.getFieldValue(object, field, false);
	}

	private Object getFieldValue(Object object, Field field, boolean force) {
		try {
			return ReflectionUtils.getFieldValue(object, field, force);
		}
		catch (Exception exception) {
			throw new UnexpectedException("Unable to get %s.%s", exception, object.getClass().getName(),
					field.getName());
		}
	}

	private void setFieldValue(Object object, Field field, Object value) {
		this.setFieldValue(object, field, value, false);
	}

	private void setFieldValue(Object object, Field field, Object value, boolean force) {
		try {
			// was ReflectionUtils.set(object, field, value)
			ReflectionUtils.setFieldValue(object, field, value, force);
		}
		catch (Exception exception) {
			throw new UnexpectedException("Unable to set %s.%s -> %s", exception, object.getClass().getName(),
					field.getName(), value.toString());
		}
	}

	private Collection<Object> castCollection(Object object) {
		if (Collection.class.isAssignableFrom(object.getClass())) {
			return (Collection<Object>) object;
		}
		throw new UnexpectedException("%s is not assignable to %s", object.getClass().getName(), Collection.class);
	}

	private <N> void logAttributeMerge(N detachedEntity, N attachedEntity, Object detachedEntityId, Field field) {
		log.debug(String.format("MERGE %s[%s].%s %s -> %s", attachedEntity.getClass().getName(),
				detachedEntityId == null ? "NEW" : detachedEntityId, field.getName(),
				this.getFieldValue(attachedEntity, field, true), this.getFieldValue(detachedEntity, field, true)));
	}

}
