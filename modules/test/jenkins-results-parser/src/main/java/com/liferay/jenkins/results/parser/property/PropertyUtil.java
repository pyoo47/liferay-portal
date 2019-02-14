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

package com.liferay.jenkins.results.parser.property;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Yen
 */
public class PropertyUtil {

	public static List<String> getPropertyNameParameters(String propertyName) {
		List<String> propertyNameParameters = new ArrayList<>();

		int start = -1;
		int depth = 0;

		for (int i = 0; i < propertyName.length(); i++) {
			char c = propertyName.charAt(i);

			if (c == '[') {
				depth++;
				start = i;
			}

			if (c == ']') {
				if (start == -1) {
					continue;
				}

				if (depth == 1) {
					propertyNameParameters.add(
						propertyName.substring(start + 1, i));
				}

				depth--;
				start = -1;
			}
		}

		return propertyNameParameters;
	}

}