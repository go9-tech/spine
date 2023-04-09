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
package tech.go9.spine.data.jpa.internal.model;

public class OperationType {

	public static final OperationType SAVE = OperationType.valueOf("SAVE");

	public static final OperationType CREATE = OperationType.valueOf(SAVE, "CREATE");

	public static final OperationType RETRIEVE = OperationType.valueOf("RETRIEVE");

	public static final OperationType UPDATE = OperationType.valueOf(SAVE, "UPDATE");

	public static final OperationType DELETE = OperationType.valueOf("DELETE");

	private OperationType parent;

	private String name;

	private OperationType(OperationType parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	public static OperationType valueOf(String name) {
		return OperationType.valueOf(null, name);
	}

	public static OperationType valueOf(OperationType parent, String name) {
		OperationType operationType = OperationTypeHolder.getInstance().get(name);
		if (operationType == null) {
			operationType = new OperationType(parent, name);
			OperationTypeHolder.getInstance().add(operationType);
		}
		return operationType;
	}

	public OperationType getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		OperationType other = (OperationType) object;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "OperationType [parent=" + parent + ", name=" + name + "]";
	}

}
