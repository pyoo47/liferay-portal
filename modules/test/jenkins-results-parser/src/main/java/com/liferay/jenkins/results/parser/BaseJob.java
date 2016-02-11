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

/**
 * @author Kevin Yen
 */
public class BaseJob {

	public BaseJob(String jobName, String masterURL) {
		this(jobName, masterURL, "");

		this.exist = false;
	}

	public BaseJob(String jobName, String masterURL, String buildURL) {
		this.jobName = jobName;
		this.masterURL = masterURL;
		this.buildURL = buildURL;
		this.exist = true;
	}

	public boolean exist() {
		return exist;
	}

	public String getBuildURL() {
		return buildURL;
	}

	public String getJobName() {
		return jobName;
	}

	public String getJobURL() {
		StringBuilder sb = new StringBuilder();

		sb.append(masterURL);
		sb.append("/job/");
		sb.append(jobName);

		return sb.toString();
	}

	public String getMasterURL() {
		return masterURL;
	}

	public void setBuildURL(String buildURL) {
		this.buildURL = buildURL;
		exist = true;
	}

	protected String buildURL;
	protected boolean exist;
	protected final String jobName;
	protected final String masterURL;

}