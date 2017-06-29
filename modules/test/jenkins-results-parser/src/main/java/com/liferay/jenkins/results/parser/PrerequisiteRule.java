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

import com.liferay.jenkins.results.parser.matcher.BuildMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Yen
 */
public class PrerequisiteRule {

	public PrerequisiteRule(
		BuildMatcher assignMatcher, String description,
		BuildMatcher discardMatcher, BuildMatcher invokeMatcher,
		BuildMatcher prerequisiteMatcher) {

		this.assignMatcher = assignMatcher;
		this.description = description;
		this.discardMatcher = discardMatcher;
		this.invokeMatcher = invokeMatcher;
		this.prerequisiteMatcher = prerequisiteMatcher;
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
		if (discardMatcher != null) {
			return discardMatcher.matches(build);
		}
		else {
			return false;
		}
	}

	public boolean shouldInvoke(Build build) {
		return invokeMatcher.matches(build);
	}

	public enum State {

		DISCARD("discard"), PENDING("pending"), INVOKE("invoke");

		@Override
		public String toString() {
			return _status;
		}

		private State(String status) {
			_status = status;
		}

		private final String _status;

	}

	protected static List<Build> getMatchingBuilds(
		List<Build> builds, BuildMatcher buildMatcher) {

		List<Build> matchingBuilds = new ArrayList<>();

		for (Build build : builds) {
			if (buildMatcher.matches(build)) {
				matchingBuilds.add(build);
			}
		}

		return matchingBuilds;
	}

	protected BuildMatcher assignMatcher;
	protected String description;
	protected BuildMatcher discardMatcher;
	protected BuildMatcher invokeMatcher;
	protected BuildMatcher prerequisiteMatcher;

}
