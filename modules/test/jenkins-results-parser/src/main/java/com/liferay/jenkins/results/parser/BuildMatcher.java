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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Yoo
 */
public class BuildMatcher {

	public void addParameterPatterns(
		Pattern parameterNamePattern, Pattern parameterValuePattern) {

		_parameterNameValuePatterns.put(
			parameterNamePattern, parameterValuePattern);
	}

	public List<Build> getMatchingBuilds(List<Build> builds) {
		if (builds == null) {
			return Collections.emptyList();
		}

		List<Build> matchingBuilds = new ArrayList<>(builds.size());

		for (Build build : builds) {
			if (matches(build)) {
				matchingBuilds.add(build);
			}
		}

		return matchingBuilds;
	}

	public boolean matches(Build build) {
		if (_clazz != null) {
			if (!matchesClass(build)) {
				return false;
			}
		}

		if (_checkHasDownstreamBuilds) {
			if (!matchesHasDownstreamBuilds(build)) {
				return false;
			}
		}

		if (_jobNamePattern != null) {
			if (!matchesJobName(build)) {
				return false;
			}
		}

		if (!matchesParameters(build)) {
			return false;
		}

		if (_resultPattern != null) {
			if (!matchesResult(build)) {
				return false;
			}
		}

		if (_statusPattern != null) {
			if (!matchesStatus(build)) {
				return false;
			}
		}

		return true;
	}

	public void setBuildType(Class<? extends Build> clazz) {
		_clazz = clazz;
	}

	public void setCheckHasDownstreamBuilds(boolean checkHasDownstreamBuilds) {
		_checkHasDownstreamBuilds = checkHasDownstreamBuilds;
	}

	public void setJobNamePattern(Pattern jobNamePattern) {
		_jobNamePattern = jobNamePattern;
	}

	public void setResultPattern(Pattern resultPattern) {
		_resultPattern = resultPattern;
	}

	public void setStatusPattern(Pattern statusPattern) {
		_statusPattern = statusPattern;
	}

	protected boolean matchesClass(Build build) {
		return _clazz.isInstance(build);
	}

	protected boolean matchesHasDownstreamBuilds(Build build) {
		if (build.getDownstreamBuildCount(null) > 0) {
			return true;
		}

		return false;
	}

	protected boolean matchesJobName(Build build) {
		Matcher namePatternMatcher = _jobNamePattern.matcher(
			build.getJobName());

		if (!namePatternMatcher.find()) {
			return false;
		}

		return true;
	}

	protected boolean matchesParameters(Build build) {
		for (Entry<Pattern, Pattern> patternEntry :
				_parameterNameValuePatterns.entrySet()) {

			Map<String, String> parameters = build.getParameters();

			Entry<String, String> matchingParameterEntry = null;

			for (Entry<String, String> parameterEntry : parameters.entrySet()) {
				Pattern parameterNamePattern = patternEntry.getKey();

				Matcher parameterNamePatternMatcher =
					parameterNamePattern.matcher(parameterEntry.getKey());

				if (!parameterNamePatternMatcher.find()) {
					continue;
				}

				Pattern parameterValuePattern = patternEntry.getValue();

				Matcher parameterValuePatternMatcher =
					parameterValuePattern.matcher(parameterEntry.getValue());

				if (!parameterValuePatternMatcher.find()) {
					continue;
				}

				matchingParameterEntry = parameterEntry;

				break;
			}

			if (matchingParameterEntry == null) {
				return false;
			}
		}

		return true;
	}

	protected boolean matchesResult(Build build) {
		String buildResult = build.getResult();

		if (buildResult == null) {
			buildResult = "";
		}

		Matcher resultMatcher = _resultPattern.matcher(buildResult);

		return resultMatcher.find();
	}

	protected boolean matchesStatus(Build build) {
		Matcher statusMatcher = _statusPattern.matcher(build.getStatus());

		return statusMatcher.find();
	}

	private boolean _checkHasDownstreamBuilds;
	private Class<? extends Build> _clazz;
	private Pattern _jobNamePattern;
	private final Map<Pattern, Pattern> _parameterNameValuePatterns =
		new HashMap<>();
	private Pattern _resultPattern;
	private Pattern _statusPattern;

}