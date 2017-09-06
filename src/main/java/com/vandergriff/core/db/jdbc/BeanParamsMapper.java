package com.vandergriff.core.db.jdbc;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;

/**
 * 
 * @author Jon Vandergriff
 *
 * @param <T>
 */
public class BeanParamsMapper<T> {

	/**
	 * Returns a non-null {@link Map} representing the field names and values.
	 * Fields should be annotated with {@link Column}
	 * 
	 * @param item
	 * @return a non-null {@link Map}
	 * @throws ParamsMapperException
	 */
	public Map<String, Object> getParamsMap(T item) throws ParamsMapperException {
		Map<String, Object> result = new HashMap<>();
		try {
			BeanInfo info = Introspector.getBeanInfo(item.getClass());
			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
				try {
					Method reader = pd.getReadMethod();
					Field f = item.getClass().getDeclaredField(pd.getName());
					Annotation[] annotations = f.getAnnotations();
					for (Annotation a : annotations) {
						if (a.annotationType().isAssignableFrom(Column.class)) {
							Column c = (Column) a;
							String columnName = c.name();
							if (reader != null) {
								result.put(columnName, reader.invoke(item));
							}
						}
					}
				} catch (NoSuchFieldException e) {
					// do nothing
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| IntrospectionException e) {
			throw new ParamsMapperException(e);
		}
		return result;
	}

	/**
	 * 
	 * @param items
	 * @return
	 * @throws ParamsMapperException
	 */
	public Collection<Map<String, Object>> asBatchValues(Collection<T> items)
			throws ParamsMapperException {
		Collection<Map<String, Object>> batchValues = new ArrayList<>();
		for (T i : items) {
			Map<String, Object> values = getParamsMap(i);
			batchValues.add(values);
		}
		return batchValues;
	}

}
