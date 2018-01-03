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

package com.liferay.jenkins.results.parser.failure.message.generator;

import com.liferay.jenkins.results.parser.Build;
import com.liferay.jenkins.results.parser.Dom4JUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.dom4j.Element;

/**
 * @author Peter Yoo
 */
public class RebaseFailureMessageGenerator extends BaseFailureMessageGenerator {

	@Override
	public String getMessage(
		String buildURL, String consoleText, Hashtable<?, ?> properties) {

		if (!consoleText.contains(_TOKEN_REBASE_END) ||
			!consoleText.contains(_TOKEN_REBASE_START)) {

			return null;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<p>Please fix <strong>rebase errors</strong> on <strong>");
		sb.append("<a href=\"https://github.com/");
		sb.append(properties.get("github.origin.name"));
		sb.append("/");
		sb.append(properties.get("repository"));
		sb.append("/tree/");
		sb.append(properties.get("github.sender.branch.name"));
		sb.append("\">");
		sb.append(properties.get("github.origin.name"));
		sb.append("/");
		sb.append(properties.get("github.sender.branch.name"));
		sb.append("</a></strong>.</p>");

		int start = consoleText.lastIndexOf(_TOKEN_REBASE_START);

		start = consoleText.lastIndexOf("\n", start);

		int end = consoleText.indexOf(_TOKEN_REBASE_END, start);

		end = consoleText.lastIndexOf("\n", end);

		sb.append(getConsoleTextSnippet(consoleText, false, start, end));

		return sb.toString();
	}

	@Override
	public Element getMessageElement(Build build) {
		String consoleText = build.getConsoleText();

		if (!consoleText.contains(_TOKEN_REBASE_START)) {
			return null;
		}

		int end = consoleText.indexOf(_TOKEN_REBASE_END);

		end = consoleText.lastIndexOf("\n", end);

		int start = consoleText.lastIndexOf(_TOKEN_REBASE_START, end);

		start = consoleText.lastIndexOf("\n", start);

		Map<String, String> repositoryGitDetails = getRepositoryGitDetails(
			consoleText.substring(start, end));

		String gitHubPullRequestNumber = repositoryGitDetails.get(
			"github.pull.request.number");

		Element baseBranchAnchorElement = getBaseBranchAnchorElement(
			build.getTopLevelBuild());

		if (!gitHubPullRequestNumber.equals("0")) {
			baseBranchAnchorElement = getBaseBranchAnchorElement(
				repositoryGitDetails);
		}

		return Dom4JUtil.getNewElement(
			"div", null,
			Dom4JUtil.getNewElement(
				"p", null, "Please fix ",
				Dom4JUtil.getNewElement("strong", null, "rebase errors"),
				" on ",
				Dom4JUtil.getNewElement(
					"strong", null, baseBranchAnchorElement),
				getConsoleTextSnippetElement(consoleText, false, start, end)));
	}

	protected Map<String, String> getRepositoryGitDetails(String consoleText) {
		try {
			StringReader stringReader = new StringReader(consoleText);

			BufferedReader bufferedReader = new BufferedReader(stringReader);

			Map<String, String> repositoryGitDetails = new HashMap<>();

			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();

				if (line.startsWith("github.")) {
					String[] pair = line.split(": ");

					repositoryGitDetails.put(pair[0], pair[1]);
				}
			}

			return repositoryGitDetails;
		}
		catch (IOException ioe) {
			throw new RuntimeException("Unable to get repository git details");
		}
	}

	private static final String _TOKEN_REBASE_END = "[PostBuildScript]";

	private static final String _TOKEN_REBASE_START =
		"Unable to fetch cache branch with following parameters";

}