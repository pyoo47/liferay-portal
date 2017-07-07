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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.liferay.jenkins.results.parser.AxisBuild;
import com.liferay.jenkins.results.parser.Build;

/**
 * @author Peter Yoo
 */
public class AxisBuildSearchCriteria extends BuildSearchCriteria {

	public AxisBuildSearchCriteria() {
		super();
		
		super.setClass(AxisBuild.class);
	}

	public void setAxisNumberPattern(Pattern axisNumberPattern) {
		this.axisNumberPattern = axisNumberPattern;
	}
	
	protected boolean axisNumberPatternMatches(AxisBuild axisBuild) {
		Matcher axisNumberMatcher =
			axisNumberPattern.matcher(axisBuild.getAxisNumber());

		return axisNumberMatcher.find();
	}

	@Override
	public void setClass(Class<?> clazz) {
		return;
	}
	
	@Override
	public boolean matches(Build build) {
		if (!super.matches(build)) {
			return false;
		}

		if (axisNumberPattern != null) {
			if (!axisNumberPatternMatches((AxisBuild) build)) {
				return false;
			}
		}
		
		return true;
	}
	
	protected Pattern axisNumberPattern;

}
