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
import java.util.List;
import java.util.regex.Matcher;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Kevin Yen
 */
public class BatchBuild extends BaseBuild {

	@Override
	public Element getGitHubMessage() {
		Element messageElement = super.getGitHubMessage();

		if (messageElement == null) {
			return messageElement;
		}

		String result = getResult();

		if (result.equals("SUCCESS")) {
			return null;
		}

		if (result.equals("ABORTED")) {
			messageElement.add(
				Dom4JUtil.toCodeSnippetElement("Build was aborted"));
		}

		Element jobResultsHeadingElement = new DefaultElement("h6");

		messageElement.add(jobResultsHeadingElement);

		jobResultsHeadingElement.addText("Job Results:");

		Element paragraphElement = new DefaultElement("p");

		messageElement.add(paragraphElement);

		int successCount = 0;
		int failCount = 0;

		Element downstreamBuildOrderedListElement = new DefaultElement("ol");

		messageElement.add(downstreamBuildOrderedListElement);

		for (Build downstreamBuild : getDownstreamBuilds(null)) {
			String downstreamBuildResult = downstreamBuild.getResult();

			if (downstreamBuildResult.equals("SUCCESS")) {
				successCount++;
			}
			else {
				failCount++;

				if (failCount < 2) {
					Element downstreamBuildListItemElement = new DefaultElement(
						"li");

					downstreamBuildOrderedListElement.add(
						downstreamBuildListItemElement);

					downstreamBuildListItemElement.add(
						downstreamBuild.getGitHubMessage());
				}
			}
		}

		paragraphElement.addText(Integer.toString(successCount));
		paragraphElement.addText(" Test");

		if (successCount != 1) {
			paragraphElement.addText("s");
		}

		paragraphElement.addText(" Passed.");
		paragraphElement.add(new DefaultElement("br"));
		paragraphElement.addText(Integer.toString(failCount));
		paragraphElement.addText(" Test");

		if (failCount != 1) {
			paragraphElement.addText("s");
		}

		paragraphElement.addText(" Failed.");

		return messageElement;
	}

	@Override
	public List<TestResult> getTestResults(String testStatus) {
		String status = getStatus();

		if (!status.equals("completed")) {
			return null;
		}

		List<TestResult> testResults = new ArrayList<>();

		JSONObject testReportJSONObject = getTestReportJSONObject();

		JSONArray childReportsJSONArray = testReportJSONObject.getJSONArray(
			"childReports");

		for (int i = 0; i < childReportsJSONArray.length(); i++) {
			JSONObject childReportJSONObject =
				childReportsJSONArray.getJSONObject(i);

			JSONObject childJSONObject = childReportJSONObject.getJSONObject(
				"child");

			String axisBuildURL = childJSONObject.getString("url");

			Matcher axisBuildURLMatcher = AxisBuild.buildURLPattern.matcher(
				axisBuildURL);

			axisBuildURLMatcher.find();

			String axisVariable = axisBuildURLMatcher.group("axisVariable");

			JSONObject resultJSONObject = childReportJSONObject.getJSONObject(
				"result");

			JSONArray suitesJSONArray = resultJSONObject.getJSONArray("suites");

			testResults.addAll(
				TestResult.getTestResults(
					getAxisBuild(axisVariable), suitesJSONArray, testStatus));
		}

		return testResults;
	}

	protected BatchBuild(String url) {
		this(url, null);
	}

	protected BatchBuild(String url, TopLevelBuild topLevelBuild) {
		super(url, topLevelBuild);
	}

	@Override
	protected List<String> findDownstreamBuildsInConsoleText() {
		return Collections.emptyList();
	}

	protected AxisBuild getAxisBuild(String axisVariable) {
		for (Build downstreamBuild : getDownstreamBuilds(null)) {
			AxisBuild downstreamAxisBuild = (AxisBuild)downstreamBuild;

			if (axisVariable.equals(downstreamAxisBuild.getAxisVariable())) {
				return downstreamAxisBuild;
			}
		}

		return null;
	}

	@Override
	protected Element getFailureMessageElement() {
		return null;
	}

	@Override
	protected Element getGitHubMessageJobResultsElement() {
		Element jobResultsElement = new DefaultElement("div");

		Element buildAnchorElement = new DefaultElement("a");

		jobResultsElement.add(buildAnchorElement);

		buildAnchorElement.addAttribute("href", getBuildURL());

		buildAnchorElement.addText(getDisplayName());

		Element jobResultsHeadingElement = new DefaultElement("h6");

		jobResultsElement.add(jobResultsHeadingElement);

		jobResultsHeadingElement.addText("Job Results:");

		Element paragraphElement = new DefaultElement("p");

		jobResultsElement.add(paragraphElement);

		int successCount = getTestCountByStatus("SUCCESS");

		paragraphElement.addText(Integer.toString(successCount));

		paragraphElement.addText(" Test");

		if (successCount != 1) {
			paragraphElement.addText("s");
		}

		paragraphElement.addText(" Passed.");
		paragraphElement.add(new DefaultElement("br"));

		int failCount = getTestCountByStatus("FAILURE");

		paragraphElement.addText(Integer.toString(failCount));

		paragraphElement.addText(" Test");

		if (failCount != 1) {
			paragraphElement.addText("s");
		}

		paragraphElement.addText(" Failed.");

		jobResultsElement.add(getFailureMessageElement());

		return jobResultsElement;
	}

	protected int getTestCountByStatus(String status) {
		JSONObject testReportJSONObject = getTestReportJSONObject();

		int failCount = testReportJSONObject.getInt("failCount");
		int skipCount = testReportJSONObject.getInt("skipCount");
		int totalCount = testReportJSONObject.getInt("totalCount");

		if (status.equals("SUCCESS")) {
			return totalCount - skipCount - failCount;
		}

		if (status.equals("FAILURE")) {
			return failCount;
		}

		throw new IllegalArgumentException(
			"Invalid result parameter: " + status);
	}

}