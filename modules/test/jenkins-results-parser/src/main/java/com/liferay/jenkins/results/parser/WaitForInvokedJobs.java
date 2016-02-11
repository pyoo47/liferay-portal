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

	public static String decodeURL(String url) {
		url = url.replace("%28", "(");
		url = url.replace("%29", ")");
		url = url.replace("%5B", "[");
		url = url.replace("%5D", "]");

		return url;
	}

	public static Map<String, String> getJobParameters(
		JSONArray jobParametersJSONArray) {

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

	public static String wait(Project project, Map<String, Object> beanShellMap)
		throws Exception {

		String topLevelSharedDir = project.getProperty("top.level.shared.dir");

		TopLevelJob topLevelJob = (TopLevelJob)beanShellMap.get(
			BEANSHELL_MAP_TOP_LEVEL_JOB_KEY);

		if (topLevelJob == null) {
			return "";
		}

		beanShellMap.remove(BEANSHELL_MAP_TOP_LEVEL_JOB_KEY);

		topLevelJob = wait(
			topLevelJob, topLevelSharedDir, 30000, 900000, 7200000);

		String completedBuildURLs = StringUtils.join(
			topLevelJob.getCompletedBuildURLs(), ",");

		return completedBuildURLs;
	}

	public static TopLevelJob wait(
			TopLevelJob topLevelJob, String topLevelSharedDir,
			long updatePeriod, long findTimeout, long completionTimeout)
		throws Exception {

		long startTime = System.currentTimeMillis();

		while (true) {
			StringBuilder sb;

			for (DownstreamJob downstreamJob :
					topLevelJob.getDownstreamJobs()) {

				sb = new StringBuilder();

				sb.append(downstreamJob.getJobURL());
				sb.append("/api/json");
				sb.append("?tree=builds[actions[parameters[name,value]]");
				sb.append(",building,number,url,result]");

				JSONObject jsonObject = JenkinsResultsParserUtil.toJSONObject(
					sb.toString(), false);

				JSONArray builds = jsonObject.getJSONArray("builds");

				for (int i = 0; i < builds.length(); i++) {
					JSONObject build = builds.getJSONObject(i);

					String possibleBuildURL = decodeURL(build.getString("url"));

					JSONArray parametersJSONArray = build.getJSONArray(
						"actions").getJSONObject(0).getJSONArray("parameters");

					Map<String, String> parameters = getJobParameters(
						parametersJSONArray);

					String possibleTopLevelSharedDir = parameters.get(
						"TOP_LEVEL_SHARED_DIR");

					if (!topLevelJob.getExistBuildURLs(
							).contains(possibleBuildURL) &&
						!downstreamJob.exist() &&
						possibleTopLevelSharedDir.equals(topLevelSharedDir)) {

						downstreamJob.setBuildURL(possibleBuildURL);

						sb = new StringBuilder();

						sb.append("Build '");
						sb.append(downstreamJob.getJobName());
						sb.append("' started at ");
						sb.append(possibleBuildURL);
						sb.append(".");

						System.out.println(sb.toString());
					}

					if (topLevelJob.getExistBuildURLs(
							).contains(possibleBuildURL) &&
						!downstreamJob.completed() &&
						!build.getBoolean("building") &&
						(build.get("result") != null)) {

						downstreamJob.setCompleted(true);

						sb = new StringBuilder();

						sb.append("Build '");
						sb.append(downstreamJob.getJobName());
						sb.append("' completed at ");
						sb.append(possibleBuildURL);
						sb.append(".");

						System.out.println(sb.toString());
					}
				}
			}

			sb = new StringBuilder();

			sb.append(topLevelJob.getCompletedBuildURLs().size());
			sb.append(" Completed / ");
			sb.append(topLevelJob.getExistBuildURLs().size());
			sb.append(" Found / ");
			sb.append(topLevelJob.getInvocationURLs().size());
			sb.append(" Invoked");

			System.out.println(sb.toString());

			long elapsedTime = System.currentTimeMillis() - startTime;

			if ((topLevelJob.getExistBuildURLs().size() !=
					topLevelJob.getInvocationURLs().size()) &&
				(elapsedTime > findTimeout)) {

				throw new TimeoutException("Unable to find downstream job");
			}
			else if ((topLevelJob.getCompletedBuildURLs().size() !=
						topLevelJob.getInvocationURLs().size()) &&
					 (elapsedTime > completionTimeout)) {

				throw new TimeoutException("Downstream job timeout");
			}
			else if (topLevelJob.getCompletedBuildURLs().size() ==
						topLevelJob.getInvocationURLs().size()) {

				break;
			}
			else {
				Thread.sleep(updatePeriod);
			}
		}

		return topLevelJob;
	}

}