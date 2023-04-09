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
package tech.go9.spine.data.jpa.api.jackson;

import org.hibernate.Hibernate;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.PersistentAttributeInterceptable;
import org.hibernate.proxy.HibernateProxy;

public class HibernateProxyFilter {

	@Override
	public boolean equals(Object object) {
		if (object instanceof HibernateProxy || object instanceof PersistentAttributeInterceptable
				|| object instanceof PersistentCollection) {
			return !Hibernate.isInitialized(object);
		}
		return false;
	}

}
