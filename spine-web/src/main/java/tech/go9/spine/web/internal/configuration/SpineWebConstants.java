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
package tech.go9.spine.web.internal.configuration;

public class SpineWebConstants {

	public static final String PROPERTIES_PREFIX = "spine.web";

	public static final String APP_BASE_PACKAGE = "tech.go9.spine.web";

	public static final String BUILD_INFO_PROPERTIES_PATH = "META-INF/build-info.properties";

	public static final String FILTER_REQUEST_PARAM_NAME = "$filter";

	public static final String PAGE_NUMBER_REQUEST_PARAM_NAME = "$pageNumber";

	public static final String PAGE_SIZE_REQUEST_PARAM_NAME = "$pageSize";

	public static final String SORT_REQUEST_PARAM_NAME = "$sort";

	public static final String EXPAND_REQUEST_PARAM_NAME = "$expand";

	public static final String EXPAND_METHOD_PARAMETER_NAME = "expand";

	public static final int DEFAULT_PAGE_NUMBER = 0;

	public static final String REQUEST_METADATA_PARAMETER = "spine-web-request-metadata";

	public static final int DEFAULT_PAGE_SIZE = 100;

	public static final boolean DEFAULT_MASK_IDS = false;

	private SpineWebConstants() {

	}

}
