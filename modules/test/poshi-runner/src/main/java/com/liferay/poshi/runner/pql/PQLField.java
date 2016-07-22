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

	public PQLField(String field, Properties properties) throws Exception {
		_field = field;
		_properties = properties;

		if (_properties.containsKey(_field)) {
			String fieldValue = _properties.getProperty(_field);

			_pqlFieldValue = PQLValueFactory.build(fieldValue);
		}
		else {
			_pqlFieldValue = PQLValueFactory.build();
		}
	}

	public PQLValue getPQLFieldValue(Properties properties) {
		return _pqlFieldValue;
	}

	private final String _field;
	private final PQLValue _pqlFieldValue;
	private final Properties _properties;

}