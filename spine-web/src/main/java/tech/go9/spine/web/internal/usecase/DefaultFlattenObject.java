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
package tech.go9.spine.web.internal.usecase;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import lombok.AllArgsConstructor;
import tech.go9.spine.web.api.usecase.FlattenObject;
import tech.go9.spine.core.api.exception.UnexpectedException;

@Component
@AllArgsConstructor
@ConditionalOnSingleCandidate(FlattenObject.class)
public class DefaultFlattenObject implements FlattenObject {

	private final ObjectMapper objectMapper;

	@Override
	public List<List<ValueNode>> execute(Object object) {
		JsonNode jsonNode = this.objectMapper.convertValue(object, JsonNode.class);
		if (jsonNode.getNodeType().compareTo(JsonNodeType.OBJECT) != 0) {
			throw new UnexpectedException("Object is not an complex object");
		}
		return execute((ObjectNode) jsonNode);
	}

	private List<List<ValueNode>> execute(ObjectNode objectNode) {

		List<List<ValueNode>> rows = new ArrayList<>();

		for (JsonNode jsonNode : objectNode) {

			if (jsonNode.getNodeType().compareTo(JsonNodeType.ARRAY) == 0) {

				List<List<ValueNode>> childRows = null;
				for (JsonNode childJsonNode : (ArrayNode) jsonNode) {
					if (childRows == null) {
						childRows = execute((ObjectNode) childJsonNode);
					}
					else {
						childRows.addAll(execute((ObjectNode) childJsonNode));
					}
				}

				if (childRows != null) {
					List<List<ValueNode>> newRows = new ArrayList<List<ValueNode>>();
					for (List<ValueNode> row : rows) {
						for (List<ValueNode> childRow : childRows) {
							List<ValueNode> newRow = new ArrayList<ValueNode>();
							newRow.addAll(row);
							newRow.addAll(childRow);
							newRows.add(newRow);
						}
					}
					rows = newRows;
				}

			}
			else if (jsonNode.getNodeType().compareTo(JsonNodeType.OBJECT) == 0) {

				List<List<ValueNode>> newRows = new ArrayList<List<ValueNode>>();
				List<List<ValueNode>> childRows = execute((ObjectNode) jsonNode);
				for (List<ValueNode> row : rows) {
					for (List<ValueNode> childRow : childRows) {
						List<ValueNode> newRow = new ArrayList<ValueNode>();
						newRow.addAll(row);
						newRow.addAll(childRow);
						newRows.add(newRow);
					}
				}
				rows = newRows;

			}
			else {

				if (rows.isEmpty()) {
					rows.add(new ArrayList<ValueNode>());
				}

				for (List<ValueNode> row : rows) {
					row.add((ValueNode) jsonNode);
				}
			}
		}

		return rows;

	}

}