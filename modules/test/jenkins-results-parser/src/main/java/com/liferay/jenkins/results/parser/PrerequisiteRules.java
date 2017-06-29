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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Kevin Yen
 */
public class PrerequisiteRules {

	public void add(PrerequisiteRule prerequisiteRule) {
		_prerequisiteRules.add(prerequisiteRule);
	}

	public List<Build> getApplicableBuilds(Build build, List<Build> allBuilds) {
		Set<Build> applicableBuilds = new HashSet<>();

		for (PrerequisiteRule prerequisiteRule : _prerequisiteRules) {
			if (prerequisiteRule.isPrerequisite(build)) {
				applicableBuilds.addAll(
					prerequisiteRule.getApplicableBuilds(allBuilds));
			}
		}

		return new ArrayList<>(applicableBuilds);
	}

	public Prerequisites getPrerequisites(Build build, List<Build> allBuilds) {
		Prerequisites prerequisites = new Prerequisites();

		for (PrerequisiteRule prerequisiteRule : _prerequisiteRules) {
			if (prerequisiteRule.isApplicable(build)) {
				List<Build> prerequisiteBuilds =
					prerequisiteRule.getPrerequisiteBuilds(allBuilds);

				for (Build prerequisiteBuild : prerequisiteBuilds) {
					prerequisites.add(
						new Prerequisite(prerequisiteBuild, prerequisiteRule));
				}
			}
		}

		return prerequisites;
	}

	public PrerequisiteState getState(Build build) {
		TopLevelBuild topLevelBuild = build.getTopLevelBuild();

		Prerequisites prerequisites = getPrerequisites(
			build, BuildUtil.getAllBuilds(topLevelBuild));

		return prerequisites.getState();
	}

	public int size() {
		return _prerequisiteRules.size();
	}

	private final List<PrerequisiteRule> _prerequisiteRules = new ArrayList<>();

}