package com.vandergriff.core.db.jdbc;

import java.util.UUID;

/**
 * Generates UUID strings for primary keys
 * 
 * @author Jon Vandergriffs
 *
 */
public class UUIDKeyGenerator implements KeyGenerator<String> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vandergriff.core.db.jdbc.KeyGenerator#getKey()
	 */
	@Override
	public String getKey() {
		return UUID.randomUUID().toString();
	}

}
