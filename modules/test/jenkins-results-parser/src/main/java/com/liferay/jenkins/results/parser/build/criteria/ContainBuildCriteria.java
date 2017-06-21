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

import java.util.Arrays;
import java.util.List;

/**
 * @author Kevin Yen
 */
public abstract class ContainBuildCriteria implements BuildCriteria {

	public ContainBuildCriteria(String unparsedText, String delimiter) {
		this.strings = Arrays.asList(unparsedText.split(
			"\\s*" + delimiter + "\\s*"));
	}

	public ContainBuildCriteria(String unparsedText) {
		this(unparsedText, ",");
	}

	protected boolean contain(String testString) {
		for (String string : strings) {
			if (testString.contains(string)) {
					return true;
			}
		}

		return false;
	}

	protected List<String> strings;

}