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
public class PQLOperatorContains extends PQLOperator {

	public PQLOperatorContains(Properties properties) {
		super(properties);
	}

	public boolean compare(PQLValue pqlValue1, PQLValue pqlValue2) {
		String value1 = pqlValue1.getValue();
		String value2 = pqlValue2.getValue();

		if ((value1 == null) || (value2 == null)) {
			return false;
		}

		return value1.contains(value2);
	}

}