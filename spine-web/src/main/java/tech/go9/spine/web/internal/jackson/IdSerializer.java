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
package tech.go9.spine.web.internal.jackson;

import java.io.IOException;

import org.hashids.Hashids;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import tech.go9.spine.web.internal.configuration.SpineWebProperties;
import tech.go9.spine.core.api.util.ApplicationContextUtils;

public class IdSerializer extends StdSerializer<Long> {

	private static final long serialVersionUID = 1L;

	private final Hashids hashids;

	private final SpineWebProperties properties;

	protected IdSerializer() {
		super(Long.class);
		this.hashids = ApplicationContextUtils.getBean(Hashids.class).orElseThrow();
		this.properties = ApplicationContextUtils.getBean(SpineWebProperties.class).orElseThrow();
	}

	@Override
	public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (this.properties.isMaskIds()) {
			gen.writeString(hashids.encode(value));
		}
		else {
			gen.writeNumber(value);
		}
	}

}
