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

import com.liferay.jenkins.results.parser.build.criteria.BuildCriteria;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Yen
 */
public class PrerequisiteRule {

	public PrerequisiteRule(
		String description, List<BuildCriteria> applicableBuildCriterias,
		List<BuildCriteria> prerequisiteBuildCriterias,
		List<BuildCriteria> completeBuildCriterias,
		List<BuildCriteria> passingBuildCriterias) {

		this.description = description;
		this.applicableBuildCriterias = applicableBuildCriterias;
		this.prerequisiteBuildCriterias = prerequisiteBuildCriterias;
		this.completeBuildCriterias = completeBuildCriterias;
		this.passingBuildCriterias = passingBuildCriterias;
	}

	public List<Build> getApplicableBuilds(List<Build> builds) {
		return getMatchingBuilds(builds, applicableBuildCriterias);
	}

	public List<Build> getCompleteBuilds(List<Build> builds) {
		return getMatchingBuilds(builds, completeBuildCriterias);
	}

	public String getDescription() {
		return description;
	}

	public List<Build> getPassingBuilds(List<Build> builds) {
		return getMatchingBuilds(builds, passingBuildCriterias);
	}

	public List<Build> getPrerequisiteBuilds(List<Build> builds) {
		return getMatchingBuilds(builds, prerequisiteBuildCriterias);
	}

	public boolean isApplicable(Build build) {
		return isMatching(build, applicableBuildCriterias);
	}

	public boolean isComplete(Build build) {
		return isMatching(build, completeBuildCriterias);
	}

	public boolean isPassing(Build build) {
		return isMatching(build, passingBuildCriterias);
	}

	public boolean isPrerequisite(Build build) {
		return isMatching(build, prerequisiteBuildCriterias);
	}

	protected static List<Build> getMatchingBuilds(
		List<Build> builds, List<BuildCriteria> buildCriterias) {

		List<Build> matchingBuilds = new ArrayList<>();

		for (Build build : builds) {
			if (isMatching(build, buildCriterias)) {
				matchingBuilds.add(build);
			}
		}

		return matchingBuilds;
	}

	protected static boolean isMatching(
		Build build, List<BuildCriteria> buildCriterias) {

		for (BuildCriteria buildCriteria : buildCriterias) {
			if (!buildCriteria.matches(build)) {
				return false;
			}
		}

		return true;
	}

	protected List<BuildCriteria> applicableBuildCriterias;
	protected List<BuildCriteria> completeBuildCriterias;
	protected String description;
	protected List<BuildCriteria> passingBuildCriterias;
	protected List<BuildCriteria> prerequisiteBuildCriterias;

}