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

import com.liferay.jenkins.results.parser.failure.message.generator.FailureMessageGenerator;
import com.liferay.jenkins.results.parser.failure.message.generator.GenericFailureMessageGenerator;
import com.liferay.jenkins.results.parser.failure.message.generator.SourceFormatFailureMessageGenerator;

import org.dom4j.Element;

/**
 * @author Cesar Polanco
 */
public class SourceFormatBuild extends TopLevelBuild {

	@Override
	public String getBaseRepositoryName() {
		return _pullRequest.getRepositoryName();
	}

	@Override
	public String getBaseRepositorySHA(String repositoryName) {
		return _pullRequest.getUpstreamBranchSHA();
	}

	@Override
	public String getBranchName() {
		return _pullRequest.getUpstreamBranchName();
	}

	@Override
	public Element[] getBuildFailureElements() {
		return new Element[] {getFailureMessageElement()};
	}

	@Override
	public List<Build> getDownstreamBuilds(String result, String status) {
		List<Build> downstreamBuilds = super(result, status);

		downstreamBuilds.add(this);

		return downstreamBuilds;
	}

	@Override
	public Element getTopGithubMessageElement() {
		update();

		Element rootElement = Dom4JUtil.getNewElement(
			"html", null, getResultElement());

		Element detailsElement = Dom4JUtil.getNewElement(
			"details", rootElement,
			Dom4JUtil.getNewElement(
				"summary", null, "Click here for more details."),
			Dom4JUtil.getNewElement("h4", null, "Base Branch:"),
			getBaseBranchDetailsElement(),
			Dom4JUtil.getNewElement("h4", null, "Sender Branch:"),
			getSenderBranchDetailsElement());

		String result = getResult();

		int successCount = 0;

		if ((result != null) && result.equals("SUCCESS")) {
			successCount++;
		}

		Dom4JUtil.addToElement(
			detailsElement, String.valueOf(successCount), " out of 1 jobs PASSED");

		if (!result.equals("SUCCESS")) {
			Dom4JUtil.addToElement(
				detailsElement, getFailedJobSummaryElement());
		}
		else if (result.equals("SUCCESS")) {
			Dom4JUtil.addToElement(
				detailsElement, getSuccessfulJobSummaryElement());
		}

		Dom4JUtil.addToElement(detailsElement, getMoreDetailsElement());

		if (!result.equals("SUCCESS")) {
			Dom4JUtil.addToElement(
				detailsElement, (Object[])getBuildFailureElements());
		}

		return rootElement;
	}

	protected SourceFormatBuild(String url) {
		this(url, null);
	}

	protected SourceFormatBuild(String url, TopLevelBuild topLevelBuild) {
		super(url, topLevelBuild);

		_pullRequest = new PullRequest(getParameterValue("PULL_REQUEST_URL"));
	}

	protected Element getCompanionBranchDetailsElement() {
		String companionBranchName = getCompanionBranchName(
			_pullRequest.getUpstreamBranchName());
		String repositoryName = _pullRequest.getRepositoryName();
		String upstreamUsername = _pullRequest.getOwnerUsername();

		String companionBranchURL = JenkinsResultsParserUtil.combine(
			"https://github.com/",
			upstreamUsername,
			"/",
			repositoryName,
			"/tree/",
			companionBranchName);

		String companionBranchSHA = getCompanionBranchSHA();

		Element companionBranchDetailsElement = Dom4JUtil.getNewElement(
			"p", null, "Branch Name: ",
			Dom4JUtil.getNewAnchorElement(companionBranchURL, companionBranchName),
			Dom4JUtil.getNewElement("br"), "Branch GIT ID: ",
			Dom4JUtil.getNewAnchorElement(
				companionBranchCommitURL, companionBranchSHA));

		return companionBranchDetailsElement;
	}

	protected String getCompanionBranchName(String currentBranchName) {
		String companionBranchName = currentBranchName.substring(
			0, currentBranchName.indexOf("-private"));

		return companionBranchName;
	}

	protected Element getDetailsElement() {
		Element detailsElement = Dom4JUtil.getNewElement(
			"details", null,
			Dom4JUtil.getNewElement(
				"summary", null, "Click here for more details."),
			Dom4JUtil.getNewElement("h4", null, "Base Branch:"),
			getBaseBranchDetailsElement(),
			Dom4JUtil.getNewElement("h4", null, "Sender Branch:"),
			getSenderBranchDetailsElement());

		if (_pullRequest.getUpstreamBranchName().contains("-private")) {
			Dom4JUtil.addToElement(
				detailsElement,
				Dom4JUtil.getNewElement("h4", null, "Companion Branch:"),
				getCompanionBranchDetailsElement());
		}
	}

	@Override
	protected FailureMessageGenerator[] getFailureMessageGenerators() {
		return new FailureMessageGenerator[] {
			new SourceFormatFailureMessageGenerator(),

			new GenericFailureMessageGenerator()
		};
	}

	protected Element getSenderBranchDetailsElement() {
		String repositoryName = _pullRequest.getRepositoryName();
		String senderBranchName = _pullRequest.getSenderBranchName();
		String senderUsername = _pullRequest.getSenderUsername();

		String senderBranchURL = JenkinsResultsParserUtil.combine(
			"https://github.com/",
			senderUsername,
			"/",
			repositoryName,
			"/tree/",
			senderBranchName);

		String senderSHA = _pullRequest.getSenderSHA();

		String senderCommitURL = JenkinsResultsParserUtil.combine(
			"https://github.com/",
			senderUsername,
			"/",
			repositoryName,
			"/commit/",
			senderSHA);

		Element senderBranchDetailsElement = Dom4JUtil.getNewElement(
			"p", null, "Branch Name: ",
			Dom4JUtil.getNewAnchorElement(senderBranchURL, senderBranchName),
			Dom4JUtil.getNewElement("br"), "Branch GIT ID: ",
			Dom4JUtil.getNewAnchorElement(senderCommitURL, senderSHA));

		return senderBranchDetailsElement;
	}

	@Override
	protected String getTestSuiteName() {
		return TEST_SUITE_NAME;
	}

	private PullRequest _pullRequest;
	private static final String TEST_SUITE_NAME = "ci:test:sf";

}