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
package tech.go9.spine.data.jpa.internal.util;

import tech.go9.spine.data.jpa.internal.model.OperationType;

public final class OperationTypeUtils {

	private OperationTypeUtils() {

	}

	public static boolean deepCompare(OperationType referenceOperationType, OperationType operationType) {
		if (referenceOperationType.getName().equals(operationType.getName())) {
			return true;
		}
		else if (operationType.getParent() != null
				&& referenceOperationType.getName().equals(operationType.getParent().getName())) {
			return true;
		}
		else if (operationType.getParent() != null) {
			return deepCompare(referenceOperationType, operationType.getParent());
		}
		return false;
	}

}
