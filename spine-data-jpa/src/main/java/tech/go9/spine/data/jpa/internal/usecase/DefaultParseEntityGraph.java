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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import jakarta.persistence.metamodel.EntityType;
import lombok.extern.slf4j.Slf4j;
import tech.go9.spine.data.jpa.api.usecase.ParseEntityGraph;

@Slf4j
public class DefaultParseEntityGraph implements ParseEntityGraph {

	@Override
	public <T> EntityGraph<T> execute(EntityManager entityManager, Class<T> type, String expression) {

		Assert.notNull(entityManager, "EntityManager must not be null!");

		log.debug("Parsing EntityGraph expression: " + expression + " for class: " + type.getName());

		EntityGraph<T> entityGraph = null;
		if (expression == null || expression.isEmpty()) {
			return entityGraph;
		}

		entityGraph = entityManager.createEntityGraph(type);
		Map<String, Object> node = new LinkedHashMap<>();
		for (String expandPath : expression.trim().split(",")) {
			expandPath = expandPath.trim();
			String[] expandPathAttributes = expandPath.split("\\.");
			Map<String, Object> nextTree = node;
			for (int i = 0, partsLength = expandPathAttributes.length; i < partsLength; i++) {
				String expandPathAttribute = expandPathAttributes[i];
				Object treenode = nextTree.get(expandPathAttribute);
				if (treenode == null) {
					if (i < partsLength - 1) {
						Map<String, Object> newNextTree = new LinkedHashMap<>();
						nextTree.put(expandPathAttribute, newNextTree);
						nextTree = newNextTree;
					}
					else {
						nextTree.put(expandPathAttribute, "attribute");
					}
				}
				else {
					if (i < partsLength - 1) {
						nextTree = (Map) treenode;
					}
				}
			}
		}

		for (Entry<String, Object> nodeEntry : node.entrySet()) {
			String key = nodeEntry.getKey();
			Object value = nodeEntry.getValue();
			if (value instanceof String && ((String) value).equals("attribute")) {
				entityGraph.addAttributeNodes(key);
			}
			else {
				// addSubgraph(entityGraph.addSubgraph(key), (Map<String, Object>) value);
				Subgraph<?> subGraph = null;
				Optional<Pair<String, String>> optionalAttributePair = getAttributePair(key);
				if (optionalAttributePair.isPresent()) {
					Pair<String, String> attributePair = optionalAttributePair.get();
					subGraph = entityGraph.addSubgraph(attributePair.getSecond(),
							getEntity(entityManager, attributePair.getFirst()));
				}
				else {
					subGraph = entityGraph.addSubgraph(key);
				}
				addSubgraph(entityManager, subGraph, (Map<String, Object>) value);
			}
		}

		log.debug("EntityGraph expression: " + expression + " successfully transformed to EntityGraph: " + entityGraph
				+ " for class: " + type.getName());

		return entityGraph;
	}

	private void addSubgraph(EntityManager entityManager, Subgraph subGraph, Map<String, Object> node) {
		for (Entry<String, Object> nodeEntry : node.entrySet()) {
			String key = nodeEntry.getKey();
			Object value = nodeEntry.getValue();
			if (value instanceof String && ((String) value).equals("attribute")) {
				Optional<Pair<String, String>> optionalAttributePair = getAttributePair(key);
				if (optionalAttributePair.isPresent()) {
					Pair<String, String> attributePair = optionalAttributePair.get();
					subGraph.addAttributeNodes(attributePair.getSecond());
				}
				else {
					subGraph.addAttributeNodes(key);
				}
			}
			else {
				// addSubgraph(subGraph.addSubgraph(key), (Map<String, Object>) value);
				Subgraph<?> subSubGraph = null;
				Optional<Pair<String, String>> optionalAttributePair = getAttributePair(key);
				if (optionalAttributePair.isPresent()) {
					Pair<String, String> attributePair = optionalAttributePair.get();
					subSubGraph = subGraph.addSubgraph(attributePair.getSecond(),
							getEntity(entityManager, attributePair.getFirst()));
				}
				else {
					subSubGraph = subGraph.addSubgraph(key);
				}
				addSubgraph(entityManager, subSubGraph, (Map<String, Object>) value);
			}
		}
	}

	private Optional<Pair<String, String>> getAttributePair(String attributeName) {
		Pattern pattern = Pattern.compile("^\\(\\((.*)\\)(.*)\\)$");
		Matcher matcher = pattern.matcher(attributeName);
		if (matcher.find()) {
			return Optional.of(Pair.of(matcher.group(1), matcher.group(2)));
		}
		return Optional.empty();
	}

	private Class<?> getEntity(EntityManager entityManager, String className) {
		for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
			Class<?> clazz = entity.getBindableJavaType();
			if (clazz.getSimpleName().equals(className)) {
				return clazz;
			}
		}
		throw new RuntimeException(String.format("Entity %s not found", className));
	}

}
