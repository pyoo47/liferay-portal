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

package com.liferay.jenkins.results.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Yen
 */
public class BuildUtil {

	public static List<Build> getAllBuilds(Build build) {
		List<Build> allBuilds = new ArrayList<>();

		allBuilds.add(build);

		allBuilds.addAll(getNestedBuilds(build));

		return allBuilds;
	}

	public static List<Build> getNestedBuilds(Build build) {
		List<Build> nestedBuilds = new ArrayList<>();

		nestedBuilds.addAll(build.getDownstreamBuilds(null));

		for (Build downstreamBuild : build.getDownstreamBuilds(null)) {
			nestedBuilds.addAll(getNestedBuilds(downstreamBuild));
		}

		return nestedBuilds;
	}

}