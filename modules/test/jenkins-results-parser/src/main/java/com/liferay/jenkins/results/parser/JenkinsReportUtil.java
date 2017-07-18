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

		Date startTime = new Date( topLevelBuild.getStartTimestamp() );

		Element startTimeElement = Dom4JUtil.getNewElement(
			"p", null, "Start Time: ",
			startTime.toLocaleString(), " - Build Time: ",
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

		Element queuedBatchElement = getBatchInfoElement(
			queuedBuilds, "Queued: " + queuedBuilds.size());

		Element startingBatchElement = getBatchInfoElement(
			startingBuilds, "Starting: " + startingBuilds.size());

		Element runningBatchElement = getBatchInfoElement(
			runningBuilds, "Running: " + runningBuilds.size());

		Element completedAbortedBatchElement = getBatchInfoElement(
			completedAbortedBuilds,
			"Completed - Aborted (Missing): " + completedAbortedBuilds.size());

		Element completedFailureBatchElement = getBatchInfoElement(
			completedFailureBuilds,
			"Completed - Failure: " + completedFailureBuilds.size());

		Element completedSuccessBatchElement = getBatchInfoElement(
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

	protected static Element getAxisInfoElement(List<Build> axisBuilds) {
		Element returnElement = Dom4JUtil.getNewElement("div");

		for (Build axisBuild : axisBuilds) {
			String axisName =
				"AXIS_VARIABLE=" + ((AxisBuild)axisBuild).getAxisNumber();

			String axisBuildURL = axisBuild.getBuildURL();

			String axisConsoleURL = axisBuildURL + "console";

			String axisTestReportURL = axisBuildURL + "testReport";

			long axisDuration = axisBuild.getDuration();

			String batchDurationString =
				JenkinsResultsParserUtil.toDurationString(axisDuration);

			long axisStartTime = axisBuild.getStartTimestamp();

			Date axisStartDate = new Date(axisStartTime);

			String status = axisBuild.getStatus();

			String result = axisBuild.getResult();

			StringBuilder sb = new StringBuilder();

			sb.append("START TIME: ");
			sb.append(axisStartDate.toLocaleString());
			sb.append(" - BUILD TIME: ");
			sb.append(batchDurationString);
			sb.append(" - ");

			if (result != null) {
				sb.append(result);
			}
			else {
				sb.append(status);
			}

			Element axisJobElement = Dom4JUtil.getNewAnchorElement(
				axisBuildURL, null, axisName);

			Element axisConsoleElement = Dom4JUtil.getNewAnchorElement(
				axisConsoleURL, null, "Console");

			Element axisTestReportElement = Dom4JUtil.getNewAnchorElement(
				axisTestReportURL, null, "Test Report");

			Element axisBuildElement = Dom4JUtil.getNewElement(
				"p style='margin-left:120px'", null,
				Dom4JUtil.getNewElement(
					"font size = '2'", null, axisJobElement, " - ",
					axisConsoleElement, " - ", axisTestReportElement, " - ",
					sb.toString()));

			returnElement.add(axisBuildElement);
		}

		return returnElement;
	}

	protected static Element getBatchInfoElement(
		List<Build> batchBuilds, String status) {

		Element returnElement = Dom4JUtil.getNewElement("div");

		returnElement.add(
			Dom4JUtil.getNewElement("font size='6'", null, status));

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

			StringBuilder sb = new StringBuilder();

			sb.append("START TIME: ");
			sb.append(batchStartDate.toLocaleString());
			sb.append(" - BUILD TIME: ");
			sb.append(batchDurationString);

			Element batchJobElement = Dom4JUtil.getNewAnchorElement(
				batchBuildURL, null, batchName);

			Element batchConsoleElement = Dom4JUtil.getNewAnchorElement(
				batchConsoleURL, null, "Console");

			Element batchTestReportElement = Dom4JUtil.getNewAnchorElement(
				batchTestReportURL, null, "Test Report");

			Element batchBuildElement = Dom4JUtil.getNewElement(
				"p style='margin-left:40px'", null,
				Dom4JUtil.getNewElement(
					"font size = '4'", null, batchJobElement, " - ",
					batchConsoleElement, " - ", batchTestReportElement, " - ",
					sb.toString()));

			List<Build> axisBuilds = batchBuild.getDownstreamBuilds(null);

			Element axisBuildElement = getAxisInfoElement(axisBuilds);

			batchBuildElement.add(axisBuildElement);

			returnElement.add(batchBuildElement);
		}

		return returnElement;
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