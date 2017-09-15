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

import java.util.List;

/**
 * @author Kevin Yen
 */
public class PrerequisiteRule {

	public PrerequisiteRule(
		BuildMatcher assignMatcher, String description,
		BuildMatcher discardMatcher, BuildMatcher invokeMatcher,
		BuildMatcher prerequisiteMatcher) {

		_assignMatcher = assignMatcher;
		_description = description;
		_discardMatcher = discardMatcher;
		_invokeMatcher = invokeMatcher;
		_prerequisiteMatcher = prerequisiteMatcher;
	}

	public List<Build> getApplicableBuilds(List<Build> builds) {
		return _assignMatcher.getMatchingBuilds(builds);
	}

	public String getDescription() {
		return _description;
	}

	public BuildMatcher getDiscardMatcher() {
		return _discardMatcher;
	}

	public BuildMatcher getInvokeMatcher() {
		return _invokeMatcher;
	}

	public List<Build> getPrerequisiteBuilds(List<Build> builds) {
		return _prerequisiteMatcher.getMatchingBuilds(builds);
	}

	public boolean isApplicable(Build build) {
		return _assignMatcher.matches(build);
	}

	public boolean isPrerequisite(Build build) {
		return _prerequisiteMatcher.matches(build);
	}

	private final BuildMatcher _assignMatcher;
	private final String _description;
	private final BuildMatcher _discardMatcher;
	private final BuildMatcher _invokeMatcher;
	private final BuildMatcher _prerequisiteMatcher;

}