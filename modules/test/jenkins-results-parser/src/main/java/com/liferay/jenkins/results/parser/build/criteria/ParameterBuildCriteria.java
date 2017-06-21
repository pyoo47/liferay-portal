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

package com.liferay.jenkins.results.parser.build.criteria;

import com.liferay.jenkins.results.parser.Build;

/**
 * @author Kevin Yen
 */
public class ParameterBuildCriteria implements BuildCriteria {

	public ParameterBuildCriteria(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public boolean matches(Build build) {
		String parameterValue = build.getParameterValue(name);

		return parameterValue.equals(value);
	}

	protected String name;
	protected String value;

}