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
public class DownstreamJob extends BaseJob {

	public DownstreamJob(String name, String masterURL, String invocationURL) {
		super(name, masterURL);

		this.invocationURL = invocationURL;
	}

	public DownstreamJob(
		String name, String masterURL, String invocationURL, String buildURL) {

		super(name, masterURL, buildURL);

		this.invocationURL = invocationURL;
	}

	public String getInvocationURL() {
		return invocationURL;
	}

	protected final String invocationURL;

}