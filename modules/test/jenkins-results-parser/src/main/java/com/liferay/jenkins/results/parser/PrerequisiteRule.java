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

import com.liferay.jenkins.results.parser.matcher.Matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Yen
 */
public class PrerequisiteRule {

	public PrerequisiteRule(
		String description, Matcher assignMatcher,
		Matcher prerequisiteMatcher, Matcher invokeMatcher,
		Matcher discardMatcher) {

		this.description = description;
		this.assignMatcher = assignMatcher;
		this.prerequisiteMatcher = prerequisiteMatcher;
		this.invokeMatcher = invokeMatcher;
		this.discardMatcher = discardMatcher;
	}

	public List<Build> getApplicableBuilds(List<Build> builds) {
		return getMatchingBuilds(builds, assignMatcher);
	}

	public String getDescription() {
		return description;
	}

	public List<Build> getPrerequisiteBuilds(List<Build> builds) {
		return getMatchingBuilds(builds, prerequisiteMatcher);
	}

	public boolean isApplicable(Build build) {
		return assignMatcher.matches(build);
	}

	public boolean isPrerequisite(Build build) {
		return prerequisiteMatcher.matches(build);
	}

	public boolean shouldDiscard(Build build) {
		return discardMatcher.matches(build);
	}

	public boolean shouldInvoke(Build build) {
		return invokeMatcher.matches(build);
	}

	protected static List<Build> getMatchingBuilds(
		List<Build> builds, Matcher matcher) {

		List<Build> matchingBuilds = new ArrayList<>();

		for (Build build : builds) {
			if (matcher.matches(build)) {
				matchingBuilds.add(build);
			}
		}

		return matchingBuilds;
	}

	protected Matcher assignMatcher;
	protected String description;
	protected Matcher discardMatcher;
	protected Matcher invokeMatcher;
	protected Matcher prerequisiteMatcher;

}