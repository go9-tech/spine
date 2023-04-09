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
package tech.go9.spine.core.api.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexpUtils {

	private RegexpUtils() {

	}

	public static Optional<String> getGroup(Pattern pattern, String input, int group) {
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			try {
				return Optional.ofNullable(matcher.group(group));
			}
			catch (Exception exception) {
				return Optional.empty();
			}
		}
		else {
			return Optional.empty();
		}
	}

}
