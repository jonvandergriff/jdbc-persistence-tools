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

import javax.persistence.Id;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.ReflectionUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Jon Vandergriff
 *
 * @param <T>
 */
public class PrimaryKeyProvider<T, K> {

	@Getter
	@Setter
	private String sql;

	@Getter
	@Setter
	private DataSource dataSource;

	@Getter
	@Setter
	private NamedParameterJdbcTemplate jdbcTemplate;

	/**
	 * 
	 * @param sql
	 * @param dataSource
	 */
	public PrimaryKeyProvider(String sql, DataSource dataSource) {
		this.sql = sql;
		this.dataSource = dataSource;
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	/**
	 * 
	 * @param items
	 * @param fieldName
	 * @return
	 * @throws PrimaryKeyProviderException
	 */
	public Collection<T> assignIds(Collection<T> items, String fieldName)
			throws PrimaryKeyProviderException {
		Collection<T> results = new ArrayList<>();
		for (T c : items) {
			results.add(assignNewId(c, fieldName));
		}
		return results;
	}

	/**
	 * 
	 * @param items
	 * @param keyGenerator
	 * @return
	 * @throws PrimaryKeyProviderException
	 */
	public Collection<T> assignIds(Collection<T> items, KeyGenerator<K> keyGenerator)
			throws PrimaryKeyProviderException {

		Collection<T> results = new ArrayList<>();
		for (T c : items) {
			try {
				BeanInfo info = Introspector.getBeanInfo(c.getClass());
				for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
					try {
						Field f = c.getClass().getDeclaredField(pd.getName());
						Annotation[] annotations = f.getAnnotations();
						for (Annotation a : annotations) {
							if (a.annotationType().isAssignableFrom(Id.class)) {
								Method setter = pd.getWriteMethod();
								if (setter != null) {
									setter.invoke(c, keyGenerator.getKey());
								}
							}
						}
					} catch (NoSuchFieldException e) {
						// do nothing
					}
				}
			} catch (IllegalAccessException | InvocationTargetException
					| IntrospectionException e) {
				ReflectionUtils.handleReflectionException(e);
			}
		}
		return results;
	}

	/**
	 * 
	 * @param item
	 * @param fieldName
	 * @return
	 * @throws PrimaryKeyProviderException
	 */
	public T assignNewId(T item, String fieldName) throws PrimaryKeyProviderException {
		Long id = getNewId();
		try {
			BeanInfo info = Introspector.getBeanInfo(item.getClass());
			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
				if (StringUtils.equalsIgnoreCase(pd.getName(), fieldName)) {
					Method setter = pd.getWriteMethod();
					if (setter != null) {
						setter.invoke(item, id);
					}
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| IntrospectionException e) {
			throw new PrimaryKeyProviderException("Error assigning a new primary key value", e);
		}
		return item;
	}

	/**
	 * 
	 * @return
	 */
	public Long getNewId() {
		Map<String, Object> params = new HashMap<String, Object>();
		Long id = getJdbcTemplate().queryForObject(sql, params, Long.class);
		return id;
	}

}
