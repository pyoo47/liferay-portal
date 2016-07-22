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

	public static PQLValue build() throws Exception {
		return build(null);
	}

	public static PQLValue build(String value) throws Exception {
		if (value == null) {
			return new PQLValueNull();
		}

		if (value.startsWith("\"") && value.endsWith("\"")) {
			value = value.substring(1, value.length() - 1);
		}

		if (value.equals("true") || value.equals("false")) {
			return new PQLValueBoolean(value);
		}

		return new PQLValueString(value);
	}

}