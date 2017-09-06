package com.vandergriff.core.db.jdbc;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;

import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.ReflectionUtils;

/**
 * {@link RowMapper} that maps a row from a result set to an instance of the
 * provided class. The provided class must use @column annotations to denote the
 * column name in the result set.
 * 
 * @author Jon Vandergriff
 *
 * @param <T>
 */
public class BeanRowMapper<T> implements RowMapper<T> {

	private Class<T> clazz;

	/**
	 * 
	 * @param clazz
	 */
	public BeanRowMapper(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		T t = BeanUtils.instantiate(clazz);
		try {

			BeanInfo info = Introspector.getBeanInfo(clazz);
			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
				Method writer = pd.getWriteMethod();
				try {
					Field f = clazz.getDeclaredField(pd.getName());
					Annotation[] annotations = f.getAnnotations();
					for (Annotation a : annotations) {
						if (a.annotationType().isAssignableFrom(Column.class)) {
							Column c = (Column) a;
							String columnName = c.name();
							if (writer != null) {
								Object obj = null;
								if (f.getType().isAssignableFrom(Long.class)) {
									obj = rs.getLong(columnName);
								} else if (f.getType().isAssignableFrom(String.class)) {
									obj = rs.getString(columnName);
								} else {
									obj = rs.getObject(columnName);
								}
								try {
									writer.invoke(t, obj);
								} catch (IllegalArgumentException e) {
									ReflectionUtils.handleReflectionException(e);
								}
							}
						}
					}
				} catch (NoSuchFieldException e) {
					// do nothing
				}
			}
		} catch (IllegalAccessException | InvocationTargetException | IntrospectionException e) {
			ReflectionUtils.handleReflectionException(e);
		}
		return t;
	}
}