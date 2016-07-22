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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Hashimoto
 */
public class PQLConditional implements PQLQueryEntityResult {

	public PQLConditional(String conditional, Properties properties)
		throws Exception {

		_conditional = conditional;
		_properties = properties;

		Pattern pattern = PQLConditionalFactory.getPattern();

		Matcher matcher = pattern.matcher(_conditional);

		if (matcher.find()) {
			_pqlField = new PQLField(matcher.group(1), _properties);
			_pqlOperator = PQLOperatorFactory.build(
				matcher.group(2), _properties);
			_pqlValue = PQLValueFactory.build(matcher.group(3));
		}
		else {
			throw new Exception("Invalid condtional!");
		}
	}

	public boolean getResult() throws Exception {
		PQLValue pqlFieldValue = _pqlField.getPQLFieldValue(_properties);

		return _pqlOperator.compare(pqlFieldValue, _pqlValue);
	}

	private final String _conditional;
	private final PQLField _pqlField;
	private final PQLOperator _pqlOperator;
	private final PQLValue _pqlValue;
	private final Properties _properties;

}