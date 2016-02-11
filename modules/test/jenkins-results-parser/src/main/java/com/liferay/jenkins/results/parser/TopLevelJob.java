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
import java.util.List;

/**
 * @author Kevin Yen
 */
public class TopLevelJob extends BaseJob {

	public TopLevelJob(String jobName, String masterURL) {
		super(jobName, masterURL);

		downstreamJobs = new ArrayList<>();
	}

	public TopLevelJob(String jobName, String masterURL, String buildURL) {
		super(jobName, masterURL, buildURL);

		downstreamJobs = new ArrayList<>();
	}

	public void add(DownstreamJob downstreamJob) {
		downstreamJobs.add(downstreamJob);
	}

	public List<String> getCompletedBuildURLs() {
		List<String> completedBuildURLs = new ArrayList<>();

		for (DownstreamJob downstreamJob : downstreamJobs) {
			if (downstreamJob.completed()) {
				completedBuildURLs.add(downstreamJob.getBuildURL());
			}
		}

		return completedBuildURLs;
	}

	public List<DownstreamJob> getDownstreamJobs() {
		return downstreamJobs;
	}

	public List<String> getExistBuildURLs() {
		List<String> existBuildURLs = new ArrayList<>();

		for (DownstreamJob downstreamJob : downstreamJobs) {
			if (downstreamJob.exist()) {
				existBuildURLs.add(downstreamJob.getBuildURL());
			}
		}

		return existBuildURLs;
	}

	public List<String> getInvocationURLs() {
		List<String> invocationURLs = new ArrayList<>();

		for (DownstreamJob downstreamJob : downstreamJobs) {
			invocationURLs.add(downstreamJob.getBuildURL());
		}

		return invocationURLs;
	}

	protected List<DownstreamJob> downstreamJobs;

}