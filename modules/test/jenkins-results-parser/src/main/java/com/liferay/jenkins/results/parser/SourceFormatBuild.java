/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import com.liferay.jenkins.results.parser.failure.message.generator.FailureMessageGenerator;
import com.liferay.jenkins.results.parser.failure.message.generator.FormatFailureMessageGenerator;
import com.liferay.jenkins.results.parser.failure.message.generator.GenericFailureMessageGenerator;
import com.liferay.jenkins.results.parser.failure.message.generator.NodeSourceFormatFailureMessageGenerator;
import com.liferay.jenkins.results.parser.failure.message.generator.PoshiValidationFailureMessageGenerator;
import com.liferay.jenkins.results.parser.failure.message.generator.RebaseFailureMessageGenerator;
import com.liferay.jenkins.results.parser.failure.message.generator.RelevantRuleValidationFailureMessageGenerator;
import com.liferay.jenkins.results.parser.failure.message.generator.SourceFormatFailureMessageGenerator;

import java.io.File;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Element;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Cesar Polanco
 */
public class SourceFormatBuild
	extends DefaultTopLevelBuild
	implements PortalBranchInformationBuild, PullRequestBuild, WorkspaceBuild {

	public boolean bypassCITestRelevant() {
		PortalWorkspaceGitRepository portalWorkspaceGitRepository =
			_getPortalWorkspaceGitRepository();

		if (portalWorkspaceGitRepository == null) {
			return false;
		}

		return portalWorkspaceGitRepository.bypassCITestRelevant();
	}

	@Override
	public String getBaseGitRepositoryName() {
		PullRequest pullRequest = getPullRequest();

		return pullRequest.getGitHubRemoteGitRepositoryName();
	}

	@Override
	public String getBaseGitRepositorySHA(String gitRepositoryName) {
		if (_baseGitRepositorySHA != null) {
			return _baseGitRepositorySHA;
		}

		if (!fromArchive) {
			Workspace workspace = getWorkspace();

			WorkspaceGitRepository primaryWorkspaceGitRepository =
				workspace.getPrimaryWorkspaceGitRepository();

			_baseGitRepositorySHA =
				primaryWorkspaceGitRepository.getBaseBranchSHA();

			return _baseGitRepositorySHA;
		}

		String consoleText = getConsoleText();

		for (String line : consoleText.split("\\s*\\n\\s*")) {
			Matcher matcher = _gitHubUpstreamBranchShaPattern.matcher(line);

			if (matcher.find()) {
				_baseGitRepositorySHA = matcher.group("sha");

				return _baseGitRepositorySHA;
			}
		}

		throw new RuntimeException(
			"Unable to find Source Format Base Git Repository SHA");
	}

	@Override
	public String getBranchName() {
		PullRequest pullRequest = getPullRequest();

		return pullRequest.getUpstreamRemoteGitBranchName();
	}

	@Override
	public Element[] getBuildFailureElements() {
		return new Element[] {getFailureMessageElement()};
	}

	@Override
	public Job.BuildProfile getBuildProfile() {
		return Job.BuildProfile.DXP;
	}

	@Override
	public BranchInformation getPortalBaseBranchInformation() {
		return null;
	}

	@Override
	public BranchInformation getPortalBranchInformation() {
		Workspace workspace = getWorkspace();

		return new WorkspaceBranchInformation(
			workspace.getPrimaryWorkspaceGitRepository());
	}

	@Override
	public PullRequest getPullRequest() {
		if (_pullRequest != null) {
			return _pullRequest;
		}

		_pullRequest = PullRequestFactory.newPullRequest(
			getParameterValue("PULL_REQUEST_URL"));

		return _pullRequest;
	}

	@Override
	public String getTestSuiteName() {
		return _NAME_TEST_SUITE;
	}

	@Override
	public Element getTopGitHubMessageElement() {
		update();

		Element detailsElement = Dom4JUtil.getNewElement(
			"details", null,
			Dom4JUtil.getNewElement(
				"summary", null, "Click here for more details."),
			Dom4JUtil.getNewElement("h4", null, "Base Branch:"),
			getBaseBranchDetailsElement(),
			Dom4JUtil.getNewElement("h4", null, "Sender Branch:"),
			getSenderBranchDetailsElement());

		String result = getResult();
		int successCount = 0;

		if (Objects.equals(result, "SUCCESS")) {
			successCount++;
		}

		Dom4JUtil.addToElement(
			detailsElement, String.valueOf(successCount), " out of ",
			String.valueOf(getDownstreamBuildCountByResult(null) + 1),
			" jobs PASSED");

		if (Objects.equals(result, "SUCCESS")) {
			Dom4JUtil.addToElement(
				detailsElement, getSuccessfulJobSummaryElement());
		}
		else {
			Dom4JUtil.addToElement(
				detailsElement, getFailedJobSummaryElement());
		}

		Dom4JUtil.addToElement(detailsElement, getMoreDetailsElement());

		if (!Objects.equals(result, "SUCCESS")) {
			Dom4JUtil.addToElement(
				detailsElement, (Object[])getBuildFailureElements());
		}

		return Dom4JUtil.getNewElement(
			"html", null, getResultElement(), _getSourceFormatVersionElement(),
			detailsElement);
	}

	@Override
	public Workspace getWorkspace() {
		PullRequest pullRequest = getPullRequest();

		Workspace workspace = WorkspaceFactory.newWorkspace(
			pullRequest.getGitRepositoryName(),
			pullRequest.getUpstreamRemoteGitBranchName(), getJobName());

		WorkspaceGitRepository workspaceGitRepository =
			workspace.getPrimaryWorkspaceGitRepository();

		workspaceGitRepository.setGitHubURL(pullRequest.getHtmlURL());

		String senderBranchSHA = getParameterValue("GITHUB_SENDER_BRANCH_SHA");

		if (JenkinsResultsParserUtil.isSHA(senderBranchSHA)) {
			workspaceGitRepository.setSenderBranchSHA(senderBranchSHA);
		}

		String upstreamBranchSHA = getParameterValue(
			"GITHUB_UPSTREAM_BRANCH_SHA");

		if (JenkinsResultsParserUtil.isSHA(upstreamBranchSHA)) {
			workspaceGitRepository.setBaseBranchSHA(upstreamBranchSHA);
		}

		return workspace;
	}

	protected SourceFormatBuild(String url) {
		this(url, null);
	}

	protected SourceFormatBuild(String url, TopLevelBuild topLevelBuild) {
		super(url, topLevelBuild);
	}

	@Override
	protected FailureMessageGenerator[] getFailureMessageGenerators() {
		return new FailureMessageGenerator[] {
			new NodeSourceFormatFailureMessageGenerator(),
			//
			new FormatFailureMessageGenerator(),
			new PoshiValidationFailureMessageGenerator(),
			new RebaseFailureMessageGenerator(),
			new RelevantRuleValidationFailureMessageGenerator(),
			new SourceFormatFailureMessageGenerator(),
			//
			new GenericFailureMessageGenerator()
		};
	}

	protected Element getSenderBranchDetailsElement() {
		PullRequest pullRequest = getPullRequest();

		String gitHubRemoteGitRepositoryName =
			pullRequest.getGitHubRemoteGitRepositoryName();
		String senderBranchName = pullRequest.getSenderBranchName();
		String senderUsername = pullRequest.getSenderUsername();

		String senderBranchURL = JenkinsResultsParserUtil.combine(
			"https://github.com/", senderUsername, "/",
			gitHubRemoteGitRepositoryName, "/tree/", senderBranchName);

		String senderSHA = pullRequest.getSenderSHA();

		String senderCommitURL = JenkinsResultsParserUtil.combine(
			"https://github.com/", senderUsername, "/",
			gitHubRemoteGitRepositoryName, "/commit/", senderSHA);

		return Dom4JUtil.getNewElement(
			"p", null, "Branch Name: ",
			Dom4JUtil.getNewAnchorElement(senderBranchURL, senderBranchName),
			Dom4JUtil.getNewElement("br"), "Branch GIT ID: ",
			Dom4JUtil.getNewAnchorElement(senderCommitURL, senderSHA));
	}

	private PortalWorkspaceGitRepository _getPortalWorkspaceGitRepository() {
		Workspace workspace = getWorkspace();

		WorkspaceGitRepository workspaceGitRepository =
			workspace.getPrimaryWorkspaceGitRepository();

		if (!(workspaceGitRepository instanceof PortalWorkspaceGitRepository)) {
			return null;
		}

		return (PortalWorkspaceGitRepository)workspaceGitRepository;
	}

	private String _getSourceFormatVersion() {
		PortalWorkspaceGitRepository portalWorkspaceGitRepository =
			_getPortalWorkspaceGitRepository();

		if (portalWorkspaceGitRepository == null) {
			return null;
		}

		File ivyXMLFile = new File(
			portalWorkspaceGitRepository.getDirectory(),
			"tools/sdk/dependencies/com.liferay.source.formatter/ivy.xml");

		if (!ivyXMLFile.exists()) {
			return null;
		}

		try {
			Document document = Dom4JUtil.parse(
				JenkinsResultsParserUtil.read(ivyXMLFile));

			Element rootElement = document.getRootElement();

			Element dependenciesElement = rootElement.element("dependencies");

			for (Element dependencyElement :
					dependenciesElement.elements("dependency")) {

				if (!Objects.equals(
						dependencyElement.attributeValue("name"),
						"com.liferay.source.formatter")) {

					continue;
				}

				return dependencyElement.attributeValue("rev");
			}

			return null;
		}
		catch (Exception exception) {
			return null;
		}
	}

	private Element _getSourceFormatVersionElement() {
		PortalWorkspaceGitRepository portalWorkspaceGitRepository =
			_getPortalWorkspaceGitRepository();

		if (portalWorkspaceGitRepository == null) {
			return Dom4JUtil.getNewElement("span");
		}

		if (_isSourceFormatBuilt()) {
			StringBuilder sb = new StringBuilder();

			sb.append("Run com.liferay.source.formatter built from ");
			sb.append(portalWorkspaceGitRepository.getSenderBranchSHA());
			sb.append(".");

			return Dom4JUtil.getNewElement("p", null, sb.toString());
		}

		String sourceFormatVersion = _getSourceFormatVersion();

		if (sourceFormatVersion == null) {
			return Dom4JUtil.getNewElement("span");
		}

		StringBuilder sb = new StringBuilder();

		sb.append("Run com.liferay.source.formatter at version ");
		sb.append(sourceFormatVersion);
		sb.append(".<br />");

		if (Objects.equals(
				portalWorkspaceGitRepository.getUpstreamBranchName(),
				"master") &&
			!_isSourceFormatReleased()) {

			sb.append("<em>The latest version has not been released yet.</em>");
		}

		return Dom4JUtil.getNewElement("p", null, sb.toString());
	}

	private boolean _isSourceFormatBuilt() {
		PortalWorkspaceGitRepository portalWorkspaceGitRepository =
			_getPortalWorkspaceGitRepository();

		if (portalWorkspaceGitRepository == null) {
			return false;
		}

		GitWorkingDirectory gitWorkingDirectory =
			portalWorkspaceGitRepository.getGitWorkingDirectory();

		for (File modifiedFile : gitWorkingDirectory.getModifiedFilesList()) {
			String modifiedFilePath = JenkinsResultsParserUtil.getCanonicalPath(
				modifiedFile);

			if (modifiedFilePath.contains("modules/util/source-formatter") ||
				modifiedFilePath.contains("source-formatter.properties")) {

				return true;
			}
		}

		return false;
	}

	private boolean _isSourceFormatReleased() {
		PortalWorkspaceGitRepository portalWorkspaceGitRepository =
			_getPortalWorkspaceGitRepository();

		if (portalWorkspaceGitRepository == null) {
			return false;
		}

		try {
			JSONArray sourceFormatterJSONArray =
				JenkinsResultsParserUtil.toJSONArray(
					JenkinsResultsParserUtil.combine(
						"https://api.github.com/repos/brianchandotcom/",
						"liferay-portal/commits?path=modules/util/",
						"source-formatter&per_page=1"));

			if (sourceFormatterJSONArray.isEmpty()) {
				return false;
			}

			JSONObject sourceFormatJSONObject =
				sourceFormatterJSONArray.getJSONObject(0);

			String commitURL = sourceFormatJSONObject.optString("url");

			if (commitURL == null) {
				return false;
			}

			JSONObject commitJSONObject = JenkinsResultsParserUtil.toJSONObject(
				commitURL);

			JSONArray filesJSONArray = commitJSONObject.optJSONArray("files");

			if ((filesJSONArray == null) || filesJSONArray.isEmpty()) {
				return false;
			}

			Set<String> filenames = new HashSet<>();

			for (int i = 0; i < filesJSONArray.length(); i++) {
				JSONObject fileJSONObject = filesJSONArray.getJSONObject(i);

				filenames.add(fileJSONObject.getString("filename"));
			}

			if (!filenames.contains(
					"modules/sdk/gradle-plugins-source-formatter/bnd.bnd") ||
				!filenames.contains("modules/util/source-formatter/bnd.bnd")) {

				return false;
			}

			return true;
		}
		catch (Exception exception) {
			return false;
		}
	}

	private static final String _NAME_TEST_SUITE = "sf";

	private static final Pattern _gitHubUpstreamBranchShaPattern =
		Pattern.compile(
			"\\[beanshell\\] GITHUB_UPSTREAM_BRANCH_SHA=" +
				"(?<sha>[0-9a-f]{7,40})");

	private String _baseGitRepositorySHA;
	private PullRequest _pullRequest;

}