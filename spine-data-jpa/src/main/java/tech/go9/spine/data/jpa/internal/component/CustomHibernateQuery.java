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
package tech.go9.spine.data.jpa.internal.component;

import java.util.Map;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.querydsl.core.QueryModifiers;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Path;
import com.querydsl.jpa.FactoryExpressionTransformer;
import com.querydsl.jpa.JPQLSerializer;
import com.querydsl.jpa.hibernate.HibernateQuery;
import com.querydsl.jpa.hibernate.HibernateUtil;

public class CustomHibernateQuery<T> extends HibernateQuery<T> {

	private static final long serialVersionUID = 1L;

	private Session session;

	public CustomHibernateQuery(Session session) {
		super(session);
		this.session = session;
	}

	@Override
	public Query<T> createQuery() {

		JPQLSerializer serializer = serialize(false);
		String queryString = serializer.toString();
		Query<T> query = this.session.createQuery(queryString, this.getType());
		HibernateUtil.setConstants(query, serializer.getConstants(), getMetadata().getParams());

		if (fetchSize > 0) {
			query.setFetchSize(fetchSize);
		}

		if (timeout > 0) {
			query.setTimeout(timeout);
		}

		if (cacheable != null) {
			query.setCacheable(cacheable);
		}

		if (cacheRegion != null) {
			query.setCacheRegion(cacheRegion);
		}

		if (comment != null) {
			query.setComment(comment);
		}

		if (readOnly != null) {
			query.setReadOnly(readOnly);
		}

		for (Map.Entry<Path<?>, LockMode> entry : lockModes.entrySet()) {
			query.setLockMode(entry.getKey().toString(), entry.getValue());
		}

		if (flushMode != null) {
			query.setHibernateFlushMode(flushMode);
		}

		QueryModifiers modifiers = getMetadata().getModifiers();
		if (modifiers != null && modifiers.isRestricting()) {
			Integer limit = modifiers.getLimitAsInteger();
			Integer offset = modifiers.getOffsetAsInteger();
			if (limit != null) {
				query.setMaxResults(limit);
			}
			if (offset != null) {
				query.setFirstResult(offset);
			}
		}

		Expression<?> projection = getMetadata().getProjection();
		if (projection instanceof FactoryExpression) {
			query.setResultTransformer(new FactoryExpressionTransformer((FactoryExpression<?>) projection));
		}

		return query;
	}

}
