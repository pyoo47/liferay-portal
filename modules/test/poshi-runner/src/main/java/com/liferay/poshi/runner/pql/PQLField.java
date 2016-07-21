/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.poshi.runner.pql;

import java.util.Properties;

/**
 * @author Michael Hashimoto
 */
public class PQLField {

	public PQLField(String field) {
		_field = field;
	}

	public String getPQLField() {
		return _field;
	}

	public PQLValue getPQLValue(Properties properties) throws Exception {
		if (properties.containsKey(_field)) {
			return PQLValueFactory.build(properties.getProperty(_field));
		}

		return PQLValueFactory.build(null);
	}

	private final String _field;

}