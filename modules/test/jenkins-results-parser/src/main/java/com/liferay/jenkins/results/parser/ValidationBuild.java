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

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.dom4j.Element;

import org.json.JSONObject;

/**
 * @author Leslie Wong
 */
public class ValidationBuild extends TopLevelBuild {

	public ValidationBuild(String url) {
		super(url);
	}

	public ValidationBuild(String url, TopLevelBuild topLevelBuild) {
		super(url, topLevelBuild);
	}

	@Override
	public void findDownstreamBuilds() {
	}

	@Override
	public String getBaseRepositoryName() {
		if (repositoryName == null) {
			Properties buildProperties = null;

			try {
				buildProperties = JenkinsResultsParserUtil.getBuildProperties();
			}
			catch (IOException ioe) {
				throw new RuntimeException(
					"Unable to get build.properties", ioe);
			}

			repositoryName = buildProperties.getProperty("env.REPOSITORY_NAME");

			if ((repositoryName != null) && !repositoryName.isEmpty()) {
				return repositoryName;
			}

			throw new RuntimeException(
				"Unable to get repository name for job " + getJobName());
		}

		return repositoryName;
	}

	@Override
	public JSONObject getTestReportJSONObject() {
		try {
			return JenkinsResultsParserUtil.toJSONObject(
				JenkinsResultsParserUtil.getLocalURL(
					getBuildURL() + "testReport/api/json"),
				false);
		}
		catch (IOException ioe) {
			throw new RuntimeException(
				"Unable to get test report JSON object", ioe);
		}
	}

	@Override
	public List<TestResult> getTestResults(String testStatus) {
		String status = getStatus();

		if (!status.equals("completed")) {
			return Collections.emptyList();
		}

		JSONObject testReportJSONObject = getTestReportJSONObject();

		return TestResult.getTestResults(
			this, testReportJSONObject.getJSONArray("suites"), testStatus);
	}

	@Override
	public Element getTopGitHubMessageElement() {
		Element rootElement = getRootElement();

		Dom4JUtil.addToElement(
			rootElement, Dom4JUtil.getNewElement("h4", null, "Task Summary:"));

		String consoleText = getConsoleText();

		String[] consoleSnippets = consoleText.split(
			"Executing subrepository task ");

		if (consoleSnippets.length > 1) {
			Dom4JUtil.addToElement(
				rootElement, getTaskSummaryListElement(consoleSnippets));
		}
		else {
			Dom4JUtil.addToElement(rootElement, getFailureMessageElement());
		}

		return rootElement;
	}

	@Override
	protected String getBaseRepositorySHA() {
		Properties buildProperties = null;

		try {
			buildProperties = JenkinsResultsParserUtil.getBuildProperties();
		}
		catch (IOException ioe) {
			throw new RuntimeException("Unable to get build.properties", ioe);
		}

		return buildProperties.getProperty("env.GITHUB_UPSTREAM_BRANCH_SHA");
	}

	@Override
	protected Element getGitHubMessageJobResultsElement() {
		return null;
	}

	@Override
	protected Element getResultElement() {
		Element resultElement = Dom4JUtil.getNewElement("h1");

		String result = getResult();

		if (!result.equals("SUCCESS")) {
			resultElement.addText("Validation FAILED.");
		}
		else {
			resultElement.addText("Validation PASSED. Running batch tests.");
		}

		return resultElement;
	}

	protected List<String> getTaskNames(String[] consoleSnippets) {
		List<String> taskNameList = new ArrayList<>();

		for (int i = 0; i < consoleSnippets.length; i++) {
			String consoleSnippet = consoleSnippets[0];

			taskNameList.add(
				consoleSnippet.substring(0, consoleSnippet.indexOf("\n")));
		}

		return taskNameList;
	}

	protected Element getTaskSummaryListElement(String[] consoleSnippets) {
		Element taskSummaryListElement = Dom4JUtil.getNewElement("ul");

		Properties buildProperties;

		try {
			buildProperties = JenkinsResultsParserUtil.getBuildProperties();
		}
		catch (IOException ioe) {
			throw new RuntimeException("Unable to get build properties");
		}

		List<String> taskNames = getTaskNames(consoleSnippets);

		for (int i = 1; i < consoleSnippets.length; i++) {
			String consoleSnippet = consoleSnippets[i];

			String taskName = consoleSnippet.substring(
				0, consoleSnippet.indexOf("\n"));

			String logFileName =
				buildProperties.getProperty("top.level.user.content.url") +
					"/" + taskName + ".log";

			Element taskSummaryElement = Dom4JUtil.getNewElement(
				"li", taskSummaryListElement,
				Dom4JUtil.getNewAnchorElement(logFileName, taskName));

			try {
				recordTaskLogFile(consoleSnippet, taskName, buildProperties);
			}
			catch (InterruptedException | IOException e) {
				throw new RuntimeException(
					"Unable to write subrepository task log", e);
			}

			if (consoleSnippet.contains(
					"A report with all the test results can be found at " +
						"test-results/html/index.html")) {

				List<TestResult> testResults = getTestResults(
					taskName, new ArrayList(taskNames), null, buildProperties);

				Element downstreamBuildOrderedListElement =
					Dom4JUtil.getNewElement("ol");

				int failCount = 0;
				int successCount = 0;

				for (TestResult testResult : testResults) {
					String status = testResult.getStatus();

					if (status.equals("PASSED")) {
						successCount++;
					}
					else if (!status.equals("SKIPPED")) {
						if (failCount < 3) {
							Dom4JUtil.addToElement(
								downstreamBuildOrderedListElement,
								testResult.getGitHubListItemElement(null));
						}
						else if (failCount == 3) {
							Dom4JUtil.addToElement(
								downstreamBuildOrderedListElement,
								Dom4JUtil.getNewElement(
									"li", null,
									Dom4JUtil.getNewAnchorElement(
										getBuildURL() + "testReport", null,
										"...")));
						}

						failCount++;
					}
				}

				if (failCount != 0) {
					Dom4JUtil.addToElement(
						taskSummaryElement, " - :x:",
						Dom4JUtil.getNewElement(
							"div", null,
							Dom4JUtil.getNewElement("h6", null, "Job Results:"),
							Dom4JUtil.getNewElement(
								"p", null, Integer.toString(successCount),
								JenkinsResultsParserUtil.getNounForm(
									successCount, " Tests", " Test"),
								" Passed.", Dom4JUtil.getNewElement("br"),
								Integer.toString(failCount),
								JenkinsResultsParserUtil.getNounForm(
									failCount, " Tests", " Test"),
								" Failed.",
								downstreamBuildOrderedListElement)));
				}
				else {
					Dom4JUtil.addToElement(
						taskSummaryElement, " - :white_check_mark:");
				}
			}
			else {
				String consoleResultMessage = "Subrepository task ";

				int x = consoleSnippet.indexOf(consoleResultMessage);

				String taskResult = consoleSnippet.substring(
					x + consoleResultMessage.length(),
					consoleSnippet.indexOf("\n", x));

				if (taskResult.contains("SUCCESSFUL")) {
					Dom4JUtil.addToElement(
						taskSummaryElement, " - :white_check_mark:");
				}
				else {
					if (taskResult.contains(": Parallel execution timed out")) {
						Dom4JUtil.addToElement(
							taskSummaryElement, " - :no_entry:");
					}
					else {
						Dom4JUtil.addToElement(taskSummaryElement, " - :x:");
					}

					GenericFailureMessageGenerator
						genericFailureMessageGenerator =
							new GenericFailureMessageGenerator();

					Dom4JUtil.addToElement(
						taskSummaryElement,
						genericFailureMessageGenerator.getMessageElement(
							consoleSnippet));
				}
			}
		}

		return taskSummaryListElement;
	}

	protected List<TestResult> getTestResults(
		String taskName, List<String> taskNames, String testStatus,
		Properties properties) {

		taskNames.remove(taskName);

		JSONObject testReportJSONObject = getTestReportJSONObject();

		List<TestResult> testResults = TestResult.getTestResults(
			this, testReportJSONObject.getJSONArray("suites"), testStatus);

		List<String> excludeClassList = new ArrayList<>();

		for (String task : taskNames) {
			String testList = properties.getProperty("test.list[" + task + "]");

			if (testList != null) {
				excludeClassList.addAll(Arrays.asList(testList.split(",")));
			}
		}

		List<TestResult> taskTestResults = new ArrayList<>();

		for (TestResult testResult : testResults) {
			String className = testResult.getClassName();

			if (!excludeClassList.contains(testResult.getClassName())) {
				taskTestResults.add(testResult);
			}
		}

		return taskTestResults;
	}

	protected void recordTaskLogFile(
			String consoleSnippet, String taskName, Properties properties)
		throws InterruptedException, IOException {

		String taskFileName = taskName + ".log";
		String topLevelMasterHostname = properties.getProperty(
			"env.TOP_LEVEL_MASTER_HOSTNAME");

		File taskLogFile = new File(taskFileName);

		JenkinsResultsParserUtil.write(taskLogFile, consoleSnippet);

		String targetDirPath = JenkinsResultsParserUtil.combine(
			"jobs/", properties.getProperty("env.TOP_LEVEL_JOB_NAME"),
			"/builds/", properties.getProperty("env.TOP_LEVEL_BUILD_NUMBER"));

		String makeDirCommand = JenkinsResultsParserUtil.combine(
			"ssh -o PasswordAuthentication=no ", topLevelMasterHostname,
			" 'mkdir -p /opt/java/jenkins/userContent/", targetDirPath, "'");
		String rsyncCommand = JenkinsResultsParserUtil.combine(
			"rsync -avz --chmod=go=rx ", taskLogFile.getCanonicalPath(), " ",
			topLevelMasterHostname, "::usercontent/", targetDirPath, "/",
			taskFileName);

		JenkinsResultsParserUtil.executeBashCommands(
			new String[] {
				JenkinsResultsParserUtil.escapeParentheses(makeDirCommand),
				JenkinsResultsParserUtil.escapeParentheses(rsyncCommand)
			});

		taskLogFile.delete();
	}

}