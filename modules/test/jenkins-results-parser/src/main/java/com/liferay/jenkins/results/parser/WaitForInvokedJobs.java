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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Project;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Kevin Yen
 */
public class WaitForInvokedJobs {

	public static final String BEANSHELL_MAP_TOP_LEVEL_JOB_KEY = "topLevelJob";

	public static String wait(Project project, Map<String, Object> beanShellMap)
		throws Exception {

		String topLevelSharedDir = project.getProperty("top.level.shared.dir");

		TopLevelJob topLevelJob = (TopLevelJob)beanShellMap.get(
			BEANSHELL_MAP_TOP_LEVEL_JOB_KEY);

		if (topLevelJob == null) {
			return "";
		}

		beanShellMap.remove(BEANSHELL_MAP_TOP_LEVEL_JOB_KEY);

		long maxQueueTime = 900000;

		if (project.getProperty("max.queue.time") != null) {
			maxQueueTime = Long.parseLong(
				project.getProperty("max.queue.time")) * 60000;
		}

		long maxWaitTime = 7200000;

		if (project.getProperty("max.wait.time") != null) {
			maxWaitTime = Long.parseLong(
				project.getProperty("max.wait.time")) * 60000;
		}

		long updatePeriod = 30000;

		if (project.getProperty("update.period") != null) {
			updatePeriod = Long.parseLong(
				project.getProperty("update.period")) * 1000;
		}

		topLevelJob = wait(
			topLevelJob, topLevelSharedDir, updatePeriod, maxQueueTime,
			maxWaitTime);

		List<DownstreamJob> completedJobs = topLevelJob.getDownstreamJobs(
			"completed");

		String completedBuildURLs = StringUtils.join(
			getBuildURLs(completedJobs), ",");

		return completedBuildURLs;
	}

	public static TopLevelJob wait(
			TopLevelJob topLevelJob, String topLevelSharedDir,
			long updatePeriod, long maxQueueTime, long maxWaitTime)
		throws Exception {

		long startTime = System.currentTimeMillis();

		while (true) {
			Map<String, List<DownstreamJob>> jobURLsMap = new HashMap<>();

			List<DownstreamJob> downstreamJobs = topLevelJob.getDownstreamJobs(
				"starting");

			downstreamJobs.addAll(topLevelJob.getDownstreamJobs("running"));

			for (DownstreamJob downstreamJob : downstreamJobs) {
				Set<String> jobURLs = jobURLsMap.keySet();

				if (jobURLs.contains(downstreamJob.getJobURL())) {
					List<DownstreamJob> jobsWithSameJobURL = jobURLsMap.get(
						downstreamJob.getJobURL());

					jobsWithSameJobURL.add(downstreamJob);
				}
				else {
					List<DownstreamJob> jobsWithSameJobURL = new ArrayList<>();

					jobsWithSameJobURL.add(downstreamJob);

					jobURLsMap.put(
						downstreamJob.getJobURL(), jobsWithSameJobURL);
				}
			}

			for (String jobURL : jobURLsMap.keySet()) {
				List<DownstreamJob> jobsWithSameJobURL = jobURLsMap.get(jobURL);

				JSONArray builds = getBuildsJSONArray(jobURL);

				for (DownstreamJob downstreamJob : jobsWithSameJobURL) {
					for (int i = 0; i < builds.length(); i++) {
						JSONObject build = builds.getJSONObject(i);

						String buildURL = decodeURL(build.getString("url"));

						if (downstreamJob.getStatus().equals("starting")) {
							Map<String, String> parameters = getJobParameters(
								build);

							List<DownstreamJob> foundJobs = getFoundJobs(
								topLevelJob);

							List<String> foundBuildURLs = getBuildURLs(
								foundJobs);

							if (!foundBuildURLs.contains(buildURL) &&
								topLevelSharedDir.equals(
									parameters.get("TOP_LEVEL_SHARED_DIR"))) {

								downstreamJob.setBuildURL(buildURL);
							}
						}

						if (downstreamJob.getStatus().equals("running") &&
							buildURL.equals(downstreamJob.getBuildURL()) &&
							(build.get("result") != null) &&
							!build.getBoolean("building")) {

							downstreamJob.setCompleted();

							break;
						}
					}
				}
			}

			System.out.print(topLevelJob.getDownstreamJobCount("completed"));
			System.out.print(" Completed / ");
			System.out.print(topLevelJob.getDownstreamJobCount("running"));
			System.out.print(" Running / ");
			System.out.print(topLevelJob.getDownstreamJobCount("starting"));
			System.out.print(" Starting / ");
			System.out.print(topLevelJob.getDownstreamJobCount());
			System.out.println(" Total");

			long elapsedTime = System.currentTimeMillis() - startTime;

			if ((elapsedTime > maxQueueTime) &&
				(getFoundJobs(topLevelJob).size() <
					topLevelJob.getDownstreamJobCount())) {

				throw new TimeoutException("Unable to find downstream job");
			}
			else if ((elapsedTime > maxWaitTime) &&
					 (topLevelJob.getDownstreamJobCount("completed") <
						 topLevelJob.getDownstreamJobCount())) {

				throw new TimeoutException("Downstream job timeout");
			}
			else if (topLevelJob.getDownstreamJobCount("completed") ==
						topLevelJob.getDownstreamJobCount()) {

				break;
			}
			else {
				Thread.sleep(updatePeriod);
			}
		}

		return topLevelJob;
	}

	private static String decodeURL(String url) {
		url = url.replace("%28", "(");
		url = url.replace("%29", ")");
		url = url.replace("%5B", "[");
		url = url.replace("%5D", "]");

		return url;
	}

	private static JSONArray getBuildsJSONArray(String jobURL)
		throws Exception {

		StringBuilder sb = new StringBuilder();

		sb.append(jobURL);
		sb.append("/api/json");
		sb.append("?tree=builds[actions[parameters[name,value]]");
		sb.append(",building,number,url,result]");

		JSONObject jsonObject = JenkinsResultsParserUtil.toJSONObject(
			sb.toString(), false);

		return jsonObject.getJSONArray("builds");
	}

	private static List<String> getBuildURLs(List<DownstreamJob> downstreamJobs)
		throws Exception {

		List<String> buildURLs = new ArrayList<>();

		for (DownstreamJob downstreamJob : downstreamJobs) {
			buildURLs.add(downstreamJob.getBuildURL());
		}

		return buildURLs;
	}

	private static List<DownstreamJob> getFoundJobs(TopLevelJob topLevelJob) {
		List<DownstreamJob> foundJobs = topLevelJob.getDownstreamJobs(
			"completed");
		foundJobs.addAll(topLevelJob.getDownstreamJobs("running"));

		return foundJobs;
	}

	private static Map<String, String> getJobParameters(
			JSONArray jobParametersJSONArray)
		throws Exception {

		Map<String, String> jobParameters = new HashMap<>();

		for (int i = 0; i < jobParametersJSONArray.length(); i++) {
			String name = jobParametersJSONArray.getJSONObject(
				i).getString("name");
			String value = jobParametersJSONArray.getJSONObject(
				i).getString("value");

			jobParameters.put(name, value);
		}

		return jobParameters;
	}

	private static Map<String, String> getJobParameters(
			JSONObject buildJSONObject)
		throws Exception {

		JSONArray parametersJSONArray = buildJSONObject.getJSONArray(
			"actions").getJSONObject(0).getJSONArray("parameters");

		return getJobParameters(parametersJSONArray);
	}

	private static void printBuildCompletedMessage(
		String name, String buildURL) {

		StringBuilder sb = new StringBuilder();

		sb.append("'");
		sb.append(name);
		sb.append("' completed at ");
		sb.append(buildURL);
		sb.append(".");

		System.out.println(sb.toString());
	}

	private static void printBuildStartedMessage(String name, String buildURL) {
		StringBuilder sb = new StringBuilder();

		sb.append("Build '");
		sb.append(name);
		sb.append("' started at ");
		sb.append(buildURL);
		sb.append(".");

		System.out.println(sb.toString());
	}

}