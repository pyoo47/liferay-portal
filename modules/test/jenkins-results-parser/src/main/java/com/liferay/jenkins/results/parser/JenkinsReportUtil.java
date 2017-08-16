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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;

/**
 * @author Kenji Heigel
 * @author Yi-Chen Tsai
 */
public class JenkinsReportUtil {

	public static Element getBasicHeaderElement(
		Build topLevelBuild, Map<String, Build> axisBuilds,
		Map<String, Build> batchBuilds, Map<String, TestResult> testResults) {

		long startTimeStamp = topLevelBuild.getStartTimestamp();

		long durationTime = topLevelBuild.getDuration();

		Date startTime = new Date(startTimeStamp);

		Element startTimeElement = Dom4JUtil.getNewElement(
			"p", null, "Start Time: ", startTime.toLocaleString(),
			" - Build Time: ",
			JenkinsResultsParserUtil.toDurationString(durationTime));

		Element ciUsageElement = getTotalCIUsageElement(axisBuilds);

		Element longestAxisElement = getLongestAxisElement(axisBuilds);

		Element longestBatchElement = getLongestBatchElement(batchBuilds);

		Element longestTestElement = getLongestTestElement(testResults);

		Element vmUsageElement = getTotalVMUSageElement(topLevelBuild);

		Element divElement = Dom4JUtil.getNewElement("div");

		divElement.add(startTimeElement);
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

		List<Build> queuedBuilds = new ArrayList();

		List<Build> startingBuilds = new ArrayList();

		List<Build> runningBuilds = new ArrayList();

		List<Build> completedAbortedBuilds = new ArrayList();

		List<Build> completedFailureBuilds = new ArrayList();

		List<Build> completedSuccessBuilds = new ArrayList();

		for (String key : batchBuildsKeySet) {
			Build build = batchBuilds.get(key);

			long buildDuration = build.getDuration();

			long batchBuildstartTime = build.getStartTimestamp();

			long buildEndTime = buildDuration + batchBuildstartTime;

			switch (build.getStatus()) {

				case "queued":
					queuedBuilds.add(build);
					break;

				case "starting":
					startingBuilds.add(build);
					break;

				case "running":
					runningBuilds.add(build);
					break;

				case "completed":
					String result = build.getResult();

					if (result.equals("SUCCESS")) {
						completedSuccessBuilds.add(build);
					}
					else if (result.equals("FAILURE") ||
							 result.equals("UNSTABLE")) {

						completedFailureBuilds.add(build);
					}
					else if (result.equals("ABORTED")) {
						completedAbortedBuilds.add(build);
					}

					break;

				case "missing":
					completedAbortedBuilds.add(build);
					break;
			}
		}

		Element queuedBatchElement = getBatchInfoTableElement(
			queuedBuilds, "Queued: " + queuedBuilds.size());

		Element startingBatchElement = getBatchInfoTableElement(
			startingBuilds, "Starting: " + startingBuilds.size());

		Element runningBatchElement = getBatchInfoTableElement(
			runningBuilds, "Running: " + runningBuilds.size());

		Element completedAbortedBatchElement = getBatchInfoTableElement(
			completedAbortedBuilds,
			"Completed - Aborted (Missing): " + completedAbortedBuilds.size());

		Element completedFailureBatchElement = getBatchInfoTableElement(
			completedFailureBuilds,
			"Completed - Failure: " + completedFailureBuilds.size());

		Element completedSuccessBatchElement = getBatchInfoTableElement(
			completedSuccessBuilds,
			"Completed - Success: " + completedSuccessBuilds.size());

		Element divElement = Dom4JUtil.getNewElement("div");

		divElement.add(queuedBatchElement);
		divElement.add(startingBatchElement);
		divElement.add(runningBatchElement);
		divElement.add(completedAbortedBatchElement);
		divElement.add(completedFailureBatchElement);
		divElement.add(completedSuccessBatchElement);

		return divElement;
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

	public static Element getHTMLHeaderElement() {
		Element headElement = Dom4JUtil.getNewElement("head");

		StringBuilder sb = new StringBuilder();

		sb.append("table, th, td { border:'0';}");

		sb.append("th { padding: 5px; text-align: left;} ");

		sb.append("td { padding: 5px; text-align: left;} ");

		sb.append("td:first-child { text-indent: 4em;} ");

		sb.append("caption { text-align: left; font-size: 150%; ");

		sb.append("white-space: nowrap;}");

		headElement.add(Dom4JUtil.getNewElement("style", null, sb.toString()));

		return headElement;
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

	protected static void addAxisInfoToTableElement(
		List<Build> axisBuilds, Element tableElement) {

		for (Build axisBuild : axisBuilds) {
			String axisName =
				"AXIS_VARIABLE=" + ((AxisBuild)axisBuild).getAxisNumber();

			String axisBuildURL = axisBuild.getBuildURL();

			String axisConsoleURL = axisBuildURL + "console";

			String axisTestReportURL = axisBuildURL + "testReport";

			long axisDuration = axisBuild.getDuration();

			String axisDurationString =
				JenkinsResultsParserUtil.toDurationString(axisDuration);

			long axisStartTime = axisBuild.getStartTimestamp();

			Date axisStartDate = new Date(axisStartTime);

			String status = axisBuild.getStatus();

			String result = axisBuild.getResult();

			Element axisNameElement = Dom4JUtil.getNewAnchorElement(
				axisBuildURL, null, axisName);

			Element axisConsoleElement = Dom4JUtil.getNewAnchorElement(
				axisConsoleURL, null, "Console");

			Element axisTestReportElement = Dom4JUtil.getNewAnchorElement(
				axisTestReportURL, null, "Test Report");

			Element tdAxisNameElement = Dom4JUtil.getNewElement("td");

			tdAxisNameElement.add(axisNameElement);

			Element tdAxisConsoleElement = Dom4JUtil.getNewElement("td");

			tdAxisConsoleElement.add(axisConsoleElement);

			Element tdAxisTestReportElement = Dom4JUtil.getNewElement("td");

			tdAxisTestReportElement.add(axisTestReportElement);

			Element tdStartTimeStringElement = Dom4JUtil.getNewElement(
				"td", null, "START TIME:");

			Element tdStartTimeElement = Dom4JUtil.getNewElement(
				"td", null, axisStartDate.toLocaleString());

			Element tdBuildTimeStringElement = Dom4JUtil.getNewElement(
				"td", null, "BUILD TIME:");

			Element tdBuildTimeElement = Dom4JUtil.getNewElement(
				"td", null, axisDurationString);

			Element tdStatusResultElement = Dom4JUtil.getNewElement("td");

			if (result != null) {
				tdStatusResultElement.addText(result);
			}
			else {
				tdStatusResultElement.addText(status);
			}

			Element trAxisInfoElement = Dom4JUtil.getNewElement("tr");

			Dom4JUtil.addToElement(
				trAxisInfoElement, tdAxisNameElement, tdAxisConsoleElement,
				tdAxisTestReportElement, tdStartTimeStringElement,
				tdStartTimeElement, tdBuildTimeStringElement,
				tdBuildTimeElement, tdStatusResultElement);

			tableElement.add(trAxisInfoElement);
		}
	}

	protected static Element getBatchInfoTableElement(
		List<Build> batchBuilds, String status) {

		Element tableElement = Dom4JUtil.getNewElement("table");

		tableElement.add(Dom4JUtil.getNewElement("caption", null, status));

		for (Build batchBuild : batchBuilds) {
			String jobName = batchBuild.getJobName();

			String batchName = batchBuild.getDisplayName();

			batchName = batchName.replace(jobName, "");

			String batchBuildURL = batchBuild.getBuildURL();

			String batchConsoleURL = batchBuildURL + "console";

			String batchTestReportURL = batchBuildURL + "testReport";

			long batchDuration = batchBuild.getDuration();

			String batchDurationString =
				JenkinsResultsParserUtil.toDurationString(batchDuration);

			long batchStartTime = batchBuild.getStartTimestamp();

			Date batchStartDate = new Date(batchStartTime);

			Element batchNameElement = Dom4JUtil.getNewAnchorElement(
				batchBuildURL, null, batchName);

			Element batchConsoleElement = Dom4JUtil.getNewAnchorElement(
				batchConsoleURL, null, "Console");

			Element batchTestReportElement = Dom4JUtil.getNewAnchorElement(
				batchTestReportURL, null, "Test Report");

			Element thBatchNameElement = Dom4JUtil.getNewElement("th");

			thBatchNameElement.add(batchNameElement);

			Element thBatchConsoleElement = Dom4JUtil.getNewElement("th");

			thBatchConsoleElement.add(batchConsoleElement);

			Element thTestReportElement = Dom4JUtil.getNewElement("th");

			thTestReportElement.add(batchTestReportElement);

			Element thStartTimeStringElement = Dom4JUtil.getNewElement(
				"th", null, "START TIME:");

			Element thStartTimeElement = Dom4JUtil.getNewElement(
				"th", null, batchStartDate.toLocaleString());

			Element thBuildTimeStringElement = Dom4JUtil.getNewElement(
				"th", null, "BUILD TIME:");

			Element thBuildTimeElement = Dom4JUtil.getNewElement(
				"th", null, batchDurationString);

			Element trBatchElement = Dom4JUtil.getNewElement("tr");

			Dom4JUtil.addToElement(
				trBatchElement, thBatchNameElement, thBatchConsoleElement,
				thTestReportElement, thStartTimeStringElement,
				thStartTimeElement, thBuildTimeStringElement,
				thBuildTimeElement);

			tableElement.add(trBatchElement);

			List<Build> axisBuilds = batchBuild.getDownstreamBuilds(null);

			addAxisInfoToTableElement(axisBuilds, tableElement);
		}

		return tableElement;
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

				String axisNumber = ((AxisBuild)axisBuild).getAxisNumber();

				longestAxisName = "AXIS_VARAIBLE=" + axisNumber;

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

		TestResult longestTestResult = null;

		Set<String> testResultsKeySet = testResults.keySet();

		for (String key : testResultsKeySet) {
			TestResult testResult = testResults.get(key);

			long testDuration = testResult.getDuration();

			if (longestTestDuration < testDuration) {
				longestTestDuration = testDuration;

				longestTestResult = testResult;
			}
		}

		if (longestTestResult != null) {
			longestTestName = longestTestResult.getDisplayName();

			longestTestURL = longestTestResult.getTestReportURL();

			Build testAxisBuild = longestTestResult.getAxisBuild();

			Build testBatchBuild = testAxisBuild.getParentBuild();

			String testBatchBuildName = testBatchBuild.getDisplayName();

			String testBatchJobName = testBatchBuild.getJobName();

			longestTestParentName = testBatchBuildName.replace(
				testBatchJobName, "");
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