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
package tech.go9.spine.core.internal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.util.Assert;

public final class MessageUtils {

	private static final Map<String, ResourceBundle> RESOURCE_BUNDLES = new HashMap<>();

	private MessageUtils() {

	}

	public static String getString(String artifactId, String key) {
		Assert.notNull(artifactId, "artifactId must not be null");
		String bundleName = String.format("%s-messages", artifactId);
		ResourceBundle resourceBundle = RESOURCE_BUNDLES.get(bundleName);

		if (resourceBundle == null) {
			resourceBundle = ResourceBundle.getBundle(bundleName);
			RESOURCE_BUNDLES.put(bundleName, resourceBundle);
		}

		try {
			return resourceBundle.getString(key);
		}
		catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

}
