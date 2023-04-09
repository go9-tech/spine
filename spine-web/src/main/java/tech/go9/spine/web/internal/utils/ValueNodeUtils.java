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
package tech.go9.spine.web.internal.utils;

import com.fasterxml.jackson.databind.node.ValueNode;

import tech.go9.spine.core.api.exception.UnexpectedException;

public final class ValueNodeUtils {

	private ValueNodeUtils() {

	}

	public static String toString(ValueNode valueNode) {
		if (valueNode.isArray()) {
			throw new UnexpectedException("Value node %s is an array", valueNode);
		}
		else if (valueNode.isBigDecimal()) {
			return valueNode.decimalValue().toString();
		}
		else if (valueNode.isBigInteger()) {
			return valueNode.bigIntegerValue().toString();
		}
		else if (valueNode.isBinary()) {
			throw new UnexpectedException("Value node %s is binary", valueNode);
		}
		else if (valueNode.isBoolean()) {
			return String.valueOf(valueNode.booleanValue());
		}
		else if (valueNode.isContainerNode()) {
			throw new UnexpectedException("Value node %s is container node", valueNode);
		}
		else if (valueNode.isDouble()) {
			return String.valueOf(valueNode.doubleValue());
		}
		else if (valueNode.isFloat()) {
			return String.valueOf(valueNode.floatValue());
		}
		else if (valueNode.isFloatingPointNumber()) {
			return String.valueOf(valueNode.floatValue());
		}
		else if (valueNode.isInt()) {
			return String.valueOf(valueNode.intValue());
		}
		else if (valueNode.isIntegralNumber()) {
			return String.valueOf(valueNode.intValue());
		}
		else if (valueNode.isLong()) {
			return String.valueOf(valueNode.longValue());
		}
		else if (valueNode.isNull()) {
			return "null";
		}
		else if (valueNode.isNumber()) {
			return String.valueOf(valueNode.numberValue());
		}
		else if (valueNode.isObject()) {
			throw new UnexpectedException("Value node %s is an object", valueNode);
		}
		else if (valueNode.isPojo()) {
			throw new UnexpectedException("Value node %s is an pojo", valueNode);
		}
		else if (valueNode.isShort()) {
			return String.valueOf(valueNode.shortValue());
		}
		else if (valueNode.isTextual()) {
			return valueNode.textValue();
		}
		throw new UnexpectedException("Unable to serialize value node %s ", valueNode);
	}

}
