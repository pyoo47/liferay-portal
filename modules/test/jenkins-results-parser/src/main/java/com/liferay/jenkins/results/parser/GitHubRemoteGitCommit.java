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

import org.apache.commons.lang.StringUtils;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class GitHubRemoteGitCommit extends BaseGitCommit {

	public String getGitHubCommitURL() {
		RemoteGitRepository remoteGitRepository =
			(RemoteGitRepository)getGitRepository();

		return JenkinsResultsParserUtil.combine(
			"https://github.com/", remoteGitRepository.getUsername(), "/",
			remoteGitRepository.getName(), "/commit/", getSHA());
	}

	public void setStatus(
		Status status, String context, String description, String targetURL) {

		JSONObject jsonObject = new JSONObject();

		jsonObject.put("state", StringUtils.lowerCase(status.toString()));

		if (context != null) {
			jsonObject.put("context", context);
		}

		if (description != null) {
			jsonObject.put("description", description);
		}

		if ((targetURL != null) && targetURL.matches("https?\\:\\/\\/.*")) {
			jsonObject.put("target_url", targetURL);
		}

		try {
			JenkinsResultsParserUtil.toJSONObject(
				getGitHubStatusURL(), jsonObject.toString());
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public enum Status {

		ERROR, FAILURE, PENDING, SUCCESS

	}

	protected GitHubRemoteGitCommit(
		String gitHubUsername, RemoteGitRepository remoteGitRepository,
		String message, String sha, Type type, long commitTime) {

		super(remoteGitRepository, message, sha, type, commitTime);

		_gitHubUsername = gitHubUsername;
	}

	protected String getGitHubStatusURL() {
		GitRepository gitRepository = getGitRepository();

		return JenkinsResultsParserUtil.getGitHubApiUrl(
			gitRepository.getName(), _gitHubUsername, "statuses/" + getSHA());
	}

	private final String _gitHubUsername;

}