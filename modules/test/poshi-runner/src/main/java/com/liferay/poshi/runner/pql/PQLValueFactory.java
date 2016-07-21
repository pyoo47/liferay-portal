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

/**
 * @author Michael Hashimoto
 */
public class PQLValueFactory {

	public static PQLValue build(String value) throws Exception {
		PQLValue pqlValue = null;

		if (value == null) {
			pqlValue = new PQLValueNull();
		}
		else if (value.equals("true") || value.equals("false")) {
			pqlValue = new PQLValueBoolean(value);
		}
		else {
			pqlValue = new PQLValueString(value);
		}

		return pqlValue;
	}

}