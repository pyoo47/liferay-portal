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

import java.io.IOException;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;

/**
 * @author Kenji Heigel
 */
public class JenkinsReportUtil {

	public static Element getBasicHeaderElement(
		Build topLevelBuild, Map<String, Build> axisBuilds,
		Map<String, Build> batchBuilds, Map<String, TestResult> testResults) {

		Element ciUsageElement = getTotalCIUsageElement(axisBuilds);

		Element longestAxisElement = getLongestAxisElement(axisBuilds);

		Element longestBatchElement = getLongestBatchElement(batchBuilds);

		Element longestTestElement = getLongestTestElement(testResults);

		Element vmUsageElement = getTotalVMUSageElement(topLevelBuild);

		Element divElement = Dom4JUtil.getNewElement("div");

		divElement.add(ciUsageElement);
		divElement.add(vmUsageElement);
		divElement.add(longestAxisElement);
		divElement.add(longestBatchElement);
		divElement.add(longestTestElement);

		return divElement;
	}

	public static Element getBatchReportElement(
		Build topLevelBuild, Map<String, Build> batchBuilds) {

		Set<String> batchBuildsKeySet = batchBuilds.keySet();

		for (String key : batchBuildsKeySet) {
			Build build = batchBuilds.get(key);

			long buildDuration = build.getDuration();
			long batchBuildstartTime = build.getStartTimestamp();

			long buildEndTime = buildDuration + batchBuildstartTime;
		}

		return null;
	}

	public static Element getChartJSScriptElement(
		String xData, String y1Data, String y2Data) {

		String resource = null;

		try {
			Class<?> clazz = JenkinsResultsParserUtil.class;

			resource = JenkinsResultsParserUtil.readInputStream(
				clazz.getResourceAsStream("chart-template.js"));
		}
		catch (IOException ioe) {
		}

		resource = resource.replace("'xData'", xData);
		resource = resource.replace("'y1Data'", y1Data);
		resource = resource.replace("'y2Data'", y2Data);

		Element scriptElement = Dom4JUtil.getNewElement("script");

		scriptElement.addText(resource);

		return scriptElement;
	}

	public static Element getTimelineElement(
		Build topLevelBuild, Map<String, Build> builds) {

		long topLevelDuration = topLevelBuild.getDuration();
		long topLevelStartTime = topLevelBuild.getStartTimestamp();

		int dataPoints = 500;

		long[] invocationData = new long[dataPoints];
		long[] slaveUsageData = new long[dataPoints];

		for (String key : builds.keySet()) {
			Build build = builds.get(key);

			long buildDuration = build.getDuration();

			long buildStartTime = build.getStartTimestamp();

			long buildEndTime = buildDuration + buildStartTime;

			long dataEnd =
				(buildEndTime - topLevelStartTime) * dataPoints /
					topLevelDuration;

			long dataStart =
				(buildStartTime - topLevelStartTime) * dataPoints /
					topLevelDuration;

			for (int i = (int)dataStart; i < dataEnd; i++) {
				slaveUsageData[i] = ++slaveUsageData[i];
			}

			invocationData[(int)dataStart] = ++invocationData[(int)dataStart];
		}

		long[] timeData = new long[dataPoints];

//		timeData[0] = topLevelStartTime;
		timeData[0] = 0;

		for (int i = 1; i < timeData.length; i++) {
			timeData[i] = timeData[0] + i * topLevelDuration / dataPoints;
		}

		Element canvasElement = Dom4JUtil.getNewElement("canvas");

		canvasElement.addAttribute("height", "300");
		canvasElement.addAttribute("id", "timeline");

		Element scriptElement = Dom4JUtil.getNewElement("script");

		scriptElement.addAttribute("src", _CHART_JS_FILE);
		scriptElement.addText("");

		Element chartJSScriptElement = getChartJSScriptElement(
			Arrays.toString(timeData), Arrays.toString(slaveUsageData),
			Arrays.toString(invocationData));

		Element divElement = Dom4JUtil.getNewElement("div");

		Dom4JUtil.addToElement(
			divElement, canvasElement, scriptElement, chartJSScriptElement);

		return divElement;
	}

	protected static Element getLongestAxisElement(
		Map<String, Build> axisBuilds) {

		long longestAxisDuration = 0;

		String longestAxisName = "Unavailable";

		String longestAxisURL = "Unavailable";

		String longestAxisParentName = "Unavailable";

		String jobName = "Unavailable";

		Set<String> axisBuildsKeySet = axisBuilds.keySet();

		for (String key : axisBuildsKeySet) {
			Build axisBuild = axisBuilds.get(key);

			long axisDuration = axisBuild.getDuration();

			if (axisDuration > longestAxisDuration) {
				longestAxisDuration = axisDuration;

				longestAxisName = axisBuild.getDisplayName();

				longestAxisParentName =
					axisBuild.getParentBuild().getDisplayName();

				jobName = axisBuild.getJobName();

				longestAxisURL = axisBuild.getBuildURL();
			}
		}

		longestAxisParentName = longestAxisParentName.replace(jobName, "");

		StringBuilder sb = new StringBuilder();

		sb.append(longestAxisParentName);

		sb.append("/");

		sb.append(longestAxisName);

		String longestAxisDisplayName = sb.toString();

		Element longestAxisElement = Dom4JUtil.getNewElement(
			"p", null, "Longest Axis: ",
			Dom4JUtil.getNewAnchorElement(
				longestAxisURL, longestAxisDisplayName),
			" in: ",
			JenkinsResultsParserUtil.toDurationString(longestAxisDuration));

		return longestAxisElement;
	}

	protected static Element getLongestBatchElement(
		Map<String, Build> batchBuilds) {

		long longestBatchDuration = 0;

		String longestBatchName = "Unavailable";

		String longestBatchURL = "Unavailable";

		String jobName = "Unavailable";

		Set<String> batchBuildsKeySet = batchBuilds.keySet();

		for (String key : batchBuildsKeySet) {
			Build build = batchBuilds.get(key);

			long batchDuration = build.getDuration();

			if (longestBatchDuration < batchDuration) {
				longestBatchDuration = batchDuration;

				jobName = build.getJobName();

				longestBatchName = build.getDisplayName();

				longestBatchURL = build.getBuildURL();
			}
		}

		String longestBatchDisplayName = longestBatchName.replace(jobName, "");

		Element longestBatchElement = Dom4JUtil.getNewElement(
			"p", null, "Longest Batch: ",
			Dom4JUtil.getNewAnchorElement(
				longestBatchURL, longestBatchDisplayName),
			" in: ",
			JenkinsResultsParserUtil.toDurationString(longestBatchDuration));

		return longestBatchElement;
	}

	protected static Element getLongestTestElement(
		Map<String, TestResult> testResults) {

		long longestTestDuration = 0;

		String longestTestName = "Unavailable";

		String longestTestURL = "Unavailable";

		String longestTestParentName = "Unavailable";

		Set<String> testResultsKeySet = testResults.keySet();

		for (String key : testResultsKeySet) {
			TestResult testResult = testResults.get(key);

			long testDuration = testResult.getDuration();

			if (longestTestDuration < testDuration) {
				longestTestDuration = testDuration;

				longestTestName = testResult.getDisplayName();

				longestTestParentName =
					testResult.getAxisBuild().getDisplayName();

				longestTestURL = testResult.getTestReportURL();
			}
		}

		StringBuilder sb = new StringBuilder();

		sb.append(longestTestParentName);

		sb.append("/");

		sb.append(longestTestName);

		String longestTestDisplayName = sb.toString();

		Element longestTestElement = Dom4JUtil.getNewElement(
			"p", null, "Longest Test: ",
			Dom4JUtil.getNewAnchorElement(
				longestTestURL, longestTestDisplayName),
			" in: ",
			JenkinsResultsParserUtil.toDurationString(longestTestDuration));

		return longestTestElement;
	}

	protected static Element getTotalCIUsageElement(
		Map<String, Build> axisBuilds) {

		long totalTime = 0;

		for (Build axisBuild : axisBuilds.values()) {
			long axisDuration = axisBuild.getDuration();

			totalTime = totalTime + axisDuration;
		}

		long hoursTotalTime = totalTime / 3600000;

		Element totalCIUsageElement = Dom4JUtil.getNewElement(
			"p", null, "Total CI Usage: " + hoursTotalTime + " server hours");

		return totalCIUsageElement;
	}

	protected static Element getTotalVMUSageElement(Build topLevelBuild) {
		long totalVMUsed = topLevelBuild.getDownstreamBuildCount(null);

		Element totalVMUsageElement = Dom4JUtil.getNewElement(
			"p", null, "Total VM used: " + totalVMUsed + " slaves");

		return totalVMUsageElement;
	}

	private static final String _CHART_JS_FILE =
		"https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.5.0/Chart.min.js";

}