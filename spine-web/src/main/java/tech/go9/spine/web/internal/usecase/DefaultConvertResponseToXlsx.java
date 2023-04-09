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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import com.fasterxml.jackson.databind.node.ValueNode;

import tech.go9.spine.web.api.usecase.ConvertResponseToOutput;
import tech.go9.spine.web.api.usecase.ConvertResponseToXlsx;
import tech.go9.spine.web.api.usecase.FlattenObject;
import tech.go9.spine.web.internal.utils.ValueNodeUtils;
import tech.go9.spine.core.api.exception.NotImplementedException;
import tech.go9.spine.core.api.exception.UnexpectedException;

//@Component
public class DefaultConvertResponseToXlsx extends AbstractGenericHttpMessageConverter<Object>
		implements ConvertResponseToOutput {

	private static final MediaType MEDIA_TYPE = new MediaType("application",
			"vnd.openxmlformats-officedocument.spreadsheetml.sheet");

	private List<ConvertResponseToXlsx<?>> converters;

	private FlattenObject flattenObject;

	public DefaultConvertResponseToXlsx(List<ConvertResponseToXlsx<?>> converters, FlattenObject flattenObject) {
		super(MEDIA_TYPE);
		this.converters = converters;
		this.flattenObject = flattenObject;
	}

	@Override
	public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException {
		return readInternal(type.getClass(), inputMessage);
	}

	@Override
	protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage) throws IOException {
		throw new NotImplementedException("Cannot read from xlsx workbook");
	}

	@Override
	protected void writeInternal(Object t, Type type, HttpOutputMessage outputMessage) throws IOException {
		UriComponents serverurl = ServletUriComponentsBuilder.fromCurrentRequest().build();
		String fileName = String.format("attachment; filename=%s.xlsx",
				serverurl.getPath().replace("/", "_").substring(1));
		outputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, fileName);
		this.execute(t, outputMessage.getBody());
	}

	@Override
	public void execute(Object object, OutputStream outputStream) {
		this.executeInternal(object, outputStream);
	}

	private <T> void executeInternal(T object, OutputStream outputStream) {
		this.getConverter((Class<T>) object.getClass()).ifPresentOrElse(
				converter -> converter.execute(object, outputStream), () -> this.write(object, outputStream));
	}

	private <T> Optional<ConvertResponseToXlsx<T>> getConverter(Class<T> type) {
		if (this.converters != null) {
			for (ConvertResponseToXlsx<?> converter : this.converters) {
				if (ResolvableType.forClass(converter.getClass()).as(ConvertResponseToXlsx.class).getGeneric(0)
						.getType().equals(type)) {
					return Optional.of((ConvertResponseToXlsx<T>) converter);
				}
			}
		}
		return Optional.empty();
	}

	private void write(Object object, OutputStream outputStream) {
		if (Resource.class.isAssignableFrom(object.getClass())) {
			Resource resource = (Resource) object;
			try {
				IOUtils.copy(resource.getInputStream(), outputStream);
			}
			catch (Exception exception) {
				throw new UnexpectedException(exception);
			}
		}
		else {
			try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
				Sheet sheet = workbook.createSheet(object.getClass().getSimpleName());
				if (Page.class.isAssignableFrom(object.getClass())) {
					writeRows(((Page<?>) object).getContent(), sheet, 0);
				}
				else if (Collection.class.isAssignableFrom(object.getClass())) {
					writeRows((Collection<?>) object, sheet, 0);
				}
				else {
					writeRow(object, sheet, 0);
				}
				try {
					workbook.write(outputStream);
				}
				catch (Exception exception) {
					throw new UnexpectedException(exception);
				}
			}
			catch (Exception exception) {
				throw new UnexpectedException(exception);
			}
		}
	}

	private void writeRows(Collection<?> objects, Sheet sheet, int currentRowNumber) {
		for (Object object : objects) {
			currentRowNumber = writeRow(object, sheet, currentRowNumber);
		}
	}

	private int writeRow(Object object, Sheet sheet, int currentRowNumber) {
		int rowNumber = currentRowNumber;
		sheet.createRow(rowNumber);
		List<List<ValueNode>> objectRows = this.flattenObject.execute(object);
		for (List<ValueNode> objectColumns : objectRows) {
			Row row = sheet.createRow(rowNumber++);
			for (int i = 0; i < objectColumns.size(); i++) {
				row.createCell(i).setCellValue(ValueNodeUtils.toString(objectColumns.get(i)));
			}
		}
		return rowNumber;
	}

}
