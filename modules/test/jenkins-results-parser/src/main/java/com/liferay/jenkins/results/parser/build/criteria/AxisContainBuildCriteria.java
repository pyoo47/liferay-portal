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

import com.liferay.jenkins.results.parser.AxisBuild;
import com.liferay.jenkins.results.parser.Build;

/**
 * @author Kevin Yen
 */
public class AxisContainBuildCriteria extends ContainBuildCriteria
	implements BuildCriteria {

	public AxisContainBuildCriteria(String unparsedText) {
		super(unparsedText);
	}

	@Override
	public boolean matches(Build build) {
		if (build instanceof AxisBuild) {
			AxisBuild axisBuild = (AxisBuild) build;

			String axisVariable = axisBuild.getAxisNumber();

			return contain(axisVariable);

		}

		return false;
	}

}