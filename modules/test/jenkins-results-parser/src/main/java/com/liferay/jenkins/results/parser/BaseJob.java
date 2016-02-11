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

	public BaseJob(String name, String masterURL) {
		this(name, masterURL, "");

		this.status = "starting";
	}

	public BaseJob(String name, String masterURL, String buildURL) {
		this.name = name;
		this.masterURL = masterURL;
		this.buildURL = buildURL;

		this.status = "running";
	}

	public String getBuildURL() {
		return buildURL;
	}

	public String getJobURL() {
		StringBuilder sb = new StringBuilder();

		sb.append(masterURL);
		sb.append("/job/");
		sb.append(name);

		return sb.toString();
	}

	public String getMasterURL() {
		return masterURL;
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public void setBuildURL(String buildURL) {
		this.buildURL = buildURL;

		if (status.equals("starting")) {
			status = "running";
		}
	}

	public void setCompleted() {
		status = "completed";
	}

	protected String buildURL;
	protected final String masterURL;
	protected final String name;
	protected String status;

}