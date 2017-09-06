package com.vandergriff.core.db.jdbc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JDBCTemplateDAOUtils {

	/**
	 * Converts the provided {@link Collection} of maps to an array of
	 * {@link HashMap}s.
	 * 
	 * @param batchValues
	 *            the collection of key/value pair maps
	 * @return a non-null hashmap array
	 */
	public static HashMap<String, Object>[] asBatchUpdateValues(
			Collection<Map<String, Object>> batchValues) {
		@SuppressWarnings("unchecked")
		HashMap<String, Object>[] map = new HashMap[batchValues.size()];
		return batchValues.toArray(map);
	}

	public static <Q, W> MapWrapper<Q, W> map(Q q, W w) {
		return new MapWrapper<Q, W>(q, w);
	}

	/**
	 * Provides easy access for creating key/value pairs for params
	 * 
	 * @author jvandergriff
	 *
	 * @param <Q>
	 * @param <W>
	 */
	public static final class MapWrapper<Q, W> {

		private final HashMap<Q, W> map;

		/**
		 * 
		 * @param q
		 * @param w
		 */
		public MapWrapper(Q q, W w) {
			map = new HashMap<Q, W>();
			map.put(q, w);
		}

		/**
		 * 
		 * @param q
		 * @param w
		 * @return
		 */
		public MapWrapper<Q, W> param(Q q, W w) {
			map.put(q, w);
			return this;
		}

		/**
		 * 
		 * @return
		 */
		public Map<Q, W> build() {
			return map;
		}
	}

}
