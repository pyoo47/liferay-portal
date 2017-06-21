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

package com.liferay.jenkins.results.parser.matcher;

import com.liferay.jenkins.results.parser.Build;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * @author Peter Yoo
 */
public class BuildMatcher {

	public void addParameterNameValuePatterns(
		Pattern parameterNamePattern, Pattern parameterValuePattern) {

		parameterNameValuePatterns.put(
			parameterNamePattern, parameterValuePattern);
	}

	public boolean matches(Build build) {
		if (clazz != null) {
			if (!classMatches(build)) {
				return false;
			}
		}

		if (checkHasDownstreamBuilds) {
			if (!hasDownstreamBuildsMatches(build)) {
				return false;
			}
		}

		if (jobNamePattern != null) {
			if (!jobNameMatches(build)) {
				return false;
			}
		}

		if (!parameterNameValueMatches(build)) {
			return false;
		}

		if (resultPattern != null) {
			if (!resultMatches(build)) {
				return false;
			}
		}

		if (statusPattern != null) {
			if (!statusMatches(build)) {
				return false;
			}
		}

		return true;
	}

	public void setBuildType(Class<? extends Build> clazz) {
		this.clazz = clazz;
	}

	public void setCheckHasDownstreamBuilds(boolean checkHasDownstreamBuilds) {
		this.checkHasDownstreamBuilds = checkHasDownstreamBuilds;
	}

	public void setJobNamePattern(Pattern jobNamePattern) {
		this.jobNamePattern = jobNamePattern;
	}

	public void setResultPattern(Pattern resultPattern) {
		this.resultPattern = resultPattern;
	}

	public void setStatusPattern(Pattern statusPattern) {
		this.statusPattern = statusPattern;
	}

	protected boolean classMatches(Build build) {
		return clazz.isInstance(build);
	}

	protected boolean hasDownstreamBuildsMatches(Build build) {
		if (build.getDownstreamBuildCount(null) > 0) {
			return true;
		}

		return false;
	}

	protected boolean jobNameMatches(Build build) {
		java.util.regex.Matcher namePatternMatcher = jobNamePattern.matcher(
			build.getJobName());

		if (!namePatternMatcher.find()) {
			return false;
		}

		return true;
	}

	protected boolean parameterNameValueMatches(Build build) {
		for (Entry<Pattern, Pattern> patternEntry :
				parameterNameValuePatterns.entrySet()) {

			Map<String, String> parameters = build.getParameters();

			Entry<String, String> matchingParameterEntry = null;

			for (Entry<String, String> parameterEntry : parameters.entrySet()) {
				Pattern parameterNamePattern = patternEntry.getKey();

				java.util.regex.Matcher parameterNamePatternMatcher =
					parameterNamePattern.matcher(parameterEntry.getKey());

				if (!parameterNamePatternMatcher.find()) {
					continue;
				}

				Pattern parameterValuePattern = patternEntry.getValue();

				java.util.regex.Matcher parameterValuePatternMatcher =
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

	protected boolean resultMatches(Build build) {
		String buildResult = build.getResult();

		if (buildResult == null) {
			buildResult = "";
		}

		java.util.regex.Matcher resultMatcher = resultPattern.matcher(
			buildResult);

		return resultMatcher.find();
	}

	protected boolean statusMatches(Build build) {
		java.util.regex.Matcher statusMatcher = statusPattern.matcher(
			build.getStatus());

		return statusMatcher.find();
	}

	protected boolean checkHasDownstreamBuilds;
	protected Class<? extends Build> clazz;
	protected Pattern jobNamePattern;
	protected Map<Pattern, Pattern> parameterNameValuePatterns =
		new HashMap<>();
	protected Pattern resultPattern;
	protected Pattern statusPattern;

}