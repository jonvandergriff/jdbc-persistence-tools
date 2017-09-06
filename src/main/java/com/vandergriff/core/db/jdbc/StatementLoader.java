package com.vandergriff.core.db.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * This is a utility class that loads the contents of an externalized SQL
 * statement defined by the given name, and located under the "sql" sub-package
 * under the package of the given class.
 * 
 * <p>
 * All resources loaded by the StatementLoader are relative to the package of
 * the <code>owner</code> class. The following sub-package structure is used:
 * 
 * <ul>
 * <li>sql -- Contains SQL statements common to supported databases</li>
 * <li>sql/<code>type</code> -- Contains statements specific to a given database
 * type, such as "oracle".</li>
 * </ul>
 * 
 * <p>
 * Below are examples of an SQL resource location for general use and one for
 * Oracle, respectively.
 * 
 * <ul>
 * <li>com/vandergriff/example/dao/person/impl/sql/load_people.sql</li>
 * <li>com/vandergriff/example/dao/person/impl/sql/oracle/update_people.sql</li>
 * </ul>
 * 
 * <p>
 * In this example, the load_people SQL is common to all, but the update_people
 * SQL is Oracle-specific.
 * 
 * @author Jon Vandergriff
 */
public class StatementLoader {

	public enum Dialect {
		ORACLE("oracle"), HSQLDB("hsqldb"), POSTGRES("postgres");

		private String dialectName;

		private Dialect(String dialectName) {
			this.dialectName = dialectName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return dialectName;
		}
	}

	/**
	 * Class owning the resources to be loaded.
	 */
	private final Class<?> owner;

	/**
	 * Type of database for the statements.
	 */
	private final Dialect dialect;

	/**
	 * Map associating logical names with the corresponding statements.
	 */
	private final Map<String, String> statements = new HashMap<String, String>();

	/**
	 * Flag indicating whether the dialect supports inline comments.
	 */
	private final boolean supportsInlineComments;

	/**
	 * Map associating owner/dialect tuples with StatementLoader instances
	 */
	private static Map<LoaderKey, StatementLoader> loaders = new HashMap<StatementLoader.LoaderKey, StatementLoader>();

	/**
	 * Simple object used as a key to look up existing StatementLoaders.
	 */
	private static class LoaderKey {

		/**
		 * The class owning the loader.
		 */
		private final Class<?> owner;

		/**
		 * The type of dialect.
		 */
		private final Dialect type;

		LoaderKey(Class<?> owner, Dialect type) {
			this.owner = owner;
			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (!(obj instanceof LoaderKey))
				return false;

			LoaderKey key = (LoaderKey) obj;

			return owner.equals(key.owner) && type.equals(key.type);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return 23 * owner.hashCode() * type.hashCode();
		}
	}

	/**
	 * Constructs a statement loader for statements owned by the given class
	 * using the given SQL dialect.
	 */
	private StatementLoader(Class<?> owner, Dialect dialect) {
		this.owner = owner;
		this.dialect = dialect;
		this.supportsInlineComments = Dialect.ORACLE == dialect;
	}

	/**
	 * Returns an instance of the StatementLoader which creates statements for
	 * the type of the given dialect.
	 * 
	 * @param owner
	 *            the class that the SQL script belongs to
	 * @param dialect
	 *            the {@link Dialect} of the SQL
	 * 
	 * @throws IllegalArgumentException
	 *             if the given data source is not defined or not of a supported
	 *             type.
	 */
	public static StatementLoader getLoader(Class<?> owner, Dialect dialect) {
		LoaderKey key = new LoaderKey(owner, dialect);

		synchronized (loaders) {
			StatementLoader loader = (StatementLoader) loaders.get(key);

			if (loader == null) {
				loader = new StatementLoader(owner, dialect);

				loaders.put(key, loader);
			}

			return loader;
		}
	}

	/**
	 * Loads the text of the given SQL statement. The statement must be included
	 * as a <code>&lt;name&gt;.sql</code> file in the SQL sub-package as the
	 * owning class.
	 * 
	 * @param name
	 *            The logical name of the statement
	 * @return The SQL statement in String form
	 * 
	 * @throws MissingResourceException
	 *             if the resource cannot be loaded.
	 */
	public String load(String name) {
		synchronized (statements) {
			// Determine if the statement is already loaded.
			String statement = (String) statements.get(name);

			// If the statement has not been loaded, do so now.
			if (statement == null) {
				statement = readStatement(name);
				statements.put(name, statement);
			}

			return statement;
		}
	}

	/**
	 * Returns the statement string with the given name.
	 * 
	 * @throws MissingResourceException
	 *             if the resource cannot be loaded.
	 */
	private String readStatement(String name) {
		StringBuilder resourceName = new StringBuilder(32);

		resourceName.append("sql/").append(dialect.toString()).append("/").append(name)
				.append(".sql");

		// First attempt to load from the dialect-specific location.
		InputStream stream = owner.getResourceAsStream(resourceName.toString());

		// If there was no .sql at that location, attempt to use the
		// dialect-independent
		// SQL package.
		if (stream == null) {
			resourceName = new StringBuilder(32);

			resourceName.append("sql/").append(name).append(".sql");

			// Attempt to load the resource of the given name.
			stream = owner.getResourceAsStream(resourceName.toString());
		}

		// If the resource still could not be found, throw an exception.
		if (stream == null) {
			throw new MissingResourceException("Unable to load SQL resource", owner.getName(),
					name);
		}

		// The SQL stream was found, so read it.
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		StringBuilder buffer = new StringBuilder(128);

		// If inline comments are supported insert comment for instrumentation.
		if (supportsInlineComments) {
			// This comment structure is intended for simple use with
			// existing SQL monitoring utilities.
			buffer.append("/*+ JDBC<");
			buffer.append(owner.getName().toUpperCase(Locale.US));
			buffer.append(">*/ ");
		}

		// Read the .sql file.
		try {
			String line = reader.readLine();

			while (line != null) {
				buffer.append(line);

				// Preserve the new lines in the externalized SQL.
				buffer.append('\n');

				line = reader.readLine();
			}
		} catch (IOException e) {
			// No IOException should occur since we are reading an internal
			// resource,
			// but propagate the exception anyway.
			throw new RuntimeException("Unable to load SQL resource" + owner.getResource(name), e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// No IOException should occur since we are reading an internal
				// resource,
				// but propagate the exception anyway.
				throw new RuntimeException("Unable to close SQL resource" + owner.getResource(name),
						e);
			}
		}

		return buffer.toString();
	}
}