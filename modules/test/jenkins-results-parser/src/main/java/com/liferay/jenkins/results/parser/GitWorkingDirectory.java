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

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.PathMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 * @author Peter Yoo
 */
public class GitWorkingDirectory {

	public void cherryPick(LocalGitCommit localGitCommit) {
		String cherryPickCommand = JenkinsResultsParserUtil.combine(
			"git cherry-pick " + localGitCommit.getSHA());

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT, cherryPickCommand);

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to cherry pick commit ", localGitCommit.getSHA(),
					"\n", executionResult.getStandardError()));
		}
	}

	public void clean() {
		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			1000 * 60 * 10, "git clean -dfx");

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to clean Git repository\n",
					executionResult.getStandardError()));
		}
	}

	public void cleanTempBranches() {
		checkoutUpstreamLocalGitBranch();

		List<String> localGitBranchNames = getLocalGitBranchNames();

		List<String> tempBranchNames = new ArrayList<>(
			localGitBranchNames.size());

		String pattern = JenkinsResultsParserUtil.combine(
			".*", Pattern.quote(getUpstreamBranchName()), "-temp", ".*");

		for (String localGitBranchName : localGitBranchNames) {
			if (localGitBranchName.matches(pattern)) {
				tempBranchNames.add(localGitBranchName);
			}
		}

		if (!tempBranchNames.isEmpty()) {
			_deleteLocalGitBranches(tempBranchNames.toArray(new String[0]));
		}
	}

	public void commitFileToCurrentBranch(String fileName, String message) {
		String commitCommand = JenkinsResultsParserUtil.combine(
			"git commit -m \"", message, "\" ", fileName);

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT, commitCommand);

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to commit file ", fileName, "\n",
					executionResult.getStandardError()));
		}
	}

	public void commitStagedFilesToCurrentBranch(String message) {
		String commitCommand = JenkinsResultsParserUtil.combine(
			"git commit -m \"", message, "\" ");

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT, commitCommand);

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to commit staged files", "\n",
					executionResult.getStandardError()));
		}
	}

	public void configure(Map<String, String> configMap, String options) {
		String[] commands = new String[configMap.size()];

		int i = 0;

		for (Map.Entry<String, String> entry : configMap.entrySet()) {
			StringBuilder sb = new StringBuilder();

			sb.append("git config ");

			if ((options != null) && !options.isEmpty()) {
				sb.append(options);
				sb.append(" ");
			}

			sb.append(entry.getKey());
			sb.append(" ");
			sb.append(entry.getValue());

			commands[i] = sb.toString();

			i++;
		}

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT, commands);

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				"Unable to configure git repository.\n" +
					executionResult.getStandardError());
		}
	}

	public void configure(
		String configName, String configValue, String options) {

		Map<String, String> configMap = new HashMap<>(1);

		configMap.put(configName, configValue);

		configure(configMap, options);
	}

	public String createPullRequest(
			String body, String pullRequestBranchName, String receiverUserName,
			String senderUserName, String title)
		throws IOException {

		JSONObject requestJSONObject = new JSONObject();

		requestJSONObject.put("base", _upstreamBranchName);
		requestJSONObject.put("body", body);
		requestJSONObject.put(
			"head", senderUserName + ":" + pullRequestBranchName);
		requestJSONObject.put("title", title);

		String url = JenkinsResultsParserUtil.getGitHubApiUrl(
			_gitRepositoryName, receiverUserName, "pulls");

		JSONObject responseJSONObject = JenkinsResultsParserUtil.toJSONObject(
			url, requestJSONObject.toString());

		String pullRequestURL = responseJSONObject.getString("html_url");

		System.out.println("Created a pull request at " + pullRequestURL);

		return pullRequestURL;
	}

	public void deleteRemoteGitBranch(RemoteGitBranch remoteGitBranch) {
		deleteRemoteGitBranches(Arrays.asList(remoteGitBranch));
	}

	public void deleteRemoteGitBranch(String branchName, GitRemote gitRemote) {
		deleteRemoteGitBranch(branchName, gitRemote.getRemoteURL());
	}

	public void deleteRemoteGitBranch(
		String branchName, RemoteGitRepository remoteGitRepository) {

		deleteRemoteGitBranch(branchName, remoteGitRepository.getRemoteURL());
	}

	public void deleteRemoteGitBranch(String branchName, String remoteURL) {
		deleteRemoteGitBranch(getRemoteGitBranch(branchName, remoteURL));
	}

	public void deleteRemoteGitBranches(
		List<RemoteGitBranch> remoteGitBranches) {

		Map<String, Set<String>> remoteURLGitBranchNameMap = new HashMap<>();

		for (RemoteGitBranch remoteGitBranch : remoteGitBranches) {
			RemoteGitRepository remoteGitRepository =
				remoteGitBranch.getRemoteGitRepository();

			String remoteURL = remoteGitRepository.getRemoteURL();

			if (!remoteURLGitBranchNameMap.containsKey(remoteURL)) {
				remoteURLGitBranchNameMap.put(remoteURL, new HashSet<String>());
			}

			Set<String> remoteGitBranchNames = remoteURLGitBranchNameMap.get(
				remoteURL);

			remoteGitBranchNames.add(remoteGitBranch.getName());

			remoteURLGitBranchNameMap.put(remoteURL, remoteGitBranchNames);
		}

		for (Map.Entry<String, Set<String>> remoteURLBranchNamesEntry :
				remoteURLGitBranchNameMap.entrySet()) {

			String remoteURL = remoteURLBranchNamesEntry.getKey();

			for (List<String> branchNames :
					Lists.partition(
						new ArrayList<String>(
							remoteURLBranchNamesEntry.getValue()),
						_BRANCHES_DELETE_BATCH_SIZE)) {

				_deleteRemoteGitBranches(
					remoteURL, branchNames.toArray(new String[0]));
			}
		}
	}

	public void displayLog() {
		displayLog(1);
	}

	public void displayLog(int logNumber) {
		String command = "git log -n " + logNumber;

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY, 1000 * 60 * 3,
			command);

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException("Unable to display log");
		}

		System.out.println();
		System.out.println(executionResult.getStandardOut());
		System.out.println();
	}

	public void fetch(GitRemote gitRemote) {
		fetch(gitRemote.getRemoteURL());
	}

	public void fetch(GitRemote gitRemote, boolean noTags) {
		fetch(gitRemote.getRemoteURL(), noTags);
	}

	public LocalGitBranch fetch(LocalGitBranch localGitBranch) {
		return fetch(null, localGitBranch);
	}

	public LocalGitBranch fetch(
		LocalGitBranch localGitBranch, boolean noTags,
		RemoteGitRef remoteGitRef) {

		if (remoteGitRef == null) {
			throw new IllegalArgumentException("Remote Git reference is null");
		}

		String remoteGitRefSHA = remoteGitRef.getSHA();

		if (localSHAExists(remoteGitRefSHA)) {
			System.out.println(
				remoteGitRefSHA + " already exists in Git repository");

			if (localGitBranch != null) {
				return createLocalGitBranch(
					localGitBranch.getName(), true, remoteGitRefSHA);
			}

			return null;
		}

		StringBuilder gitBranchesSHAReportStringBuilder = new StringBuilder();

		gitBranchesSHAReportStringBuilder.append(
			_getLocalGitBranchesSHAReport());
		gitBranchesSHAReportStringBuilder.append("\nRemote Git branch\n    ");
		gitBranchesSHAReportStringBuilder.append(remoteGitRef.getName());
		gitBranchesSHAReportStringBuilder.append(": ");
		gitBranchesSHAReportStringBuilder.append(remoteGitRef.getSHA());

		RemoteGitRepository remoteGitRepository =
			remoteGitRef.getRemoteGitRepository();

		String remoteURL = remoteGitRepository.getRemoteURL();

		if (JenkinsResultsParserUtil.isCINode() &&
			remoteURL.contains("github.com:liferay/")) {

			String gitHubDevRemoteURL = remoteURL.replace(
				"github.com:liferay/", "github-dev.liferay.com:liferay/");

			RemoteGitBranch gitHubDevRemoteGitBranch = getRemoteGitBranch(
				remoteGitRef.getName(), gitHubDevRemoteURL);

			if (gitHubDevRemoteGitBranch != null) {
				fetch(null, noTags, gitHubDevRemoteGitBranch);

				if (localSHAExists(remoteGitRefSHA)) {
					if (localGitBranch != null) {
						return createLocalGitBranch(
							localGitBranch.getName(), true, remoteGitRefSHA);
					}

					return null;
				}
			}
		}

		StringBuilder sb = new StringBuilder();

		sb.append("git fetch --progress -v -f ");

		if (noTags) {
			sb.append("--no-tags ");
		}
		else {
			sb.append("--tags ");
		}

		sb.append(remoteURL);

		String remoteGitRefName = remoteGitRef.getName();

		if ((remoteGitRefName != null) && !remoteGitRefName.isEmpty()) {
			sb.append(" ");
			sb.append(remoteGitRefName);

			if (localGitBranch != null) {
				sb.append(":");
				sb.append(localGitBranch.getName());
			}
		}

		long start = System.currentTimeMillis();

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			3, GitUtil.MILLIS_RETRY_DELAY, 1000 * 60 * 15, sb.toString());

		if (executionResult.getExitValue() != 0) {
			System.out.println(gitBranchesSHAReportStringBuilder.toString());

			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to fetch remote branch ", remoteGitRefName, "\n",
					executionResult.getStandardError()));
		}

		long duration = System.currentTimeMillis() - start;

		System.out.println(
			"Fetch completed in " +
				JenkinsResultsParserUtil.toDurationString(duration));

		if (duration > (1000 * 60)) {
			System.out.println(gitBranchesSHAReportStringBuilder.toString());
		}

		if (localSHAExists(remoteGitRefSHA) && (localGitBranch != null)) {
			return createLocalGitBranch(
				localGitBranch.getName(), true, remoteGitRefSHA);
		}

		return null;
	}

	public LocalGitBranch fetch(
		LocalGitBranch localGitBranch, RemoteGitBranch remoteGitBranch) {

		return fetch(localGitBranch, true, remoteGitBranch);
	}

	public LocalGitBranch fetch(RemoteGitBranch remoteGitBranch) {
		return fetch(null, true, remoteGitBranch);
	}

	public void fetch(RemoteGitRepository remoteGitRepository) {
		fetch(remoteGitRepository.getRemoteURL());
	}

	public void fetch(RemoteGitRepository remoteGitRepository, boolean noTags) {
		fetch(remoteGitRepository.getRemoteURL(), noTags);
	}

	public void fetch(String remoteURL) {
		fetch(remoteURL, true);
	}

	public void fetch(String remoteURL, boolean noTags) {
		if (remoteURL == null) {
			throw new IllegalArgumentException("Remote URL is null");
		}

		if (!GitUtil.isValidRemoteURL(remoteURL)) {
			throw new IllegalArgumentException(
				"Invalid remote url " + remoteURL);
		}

		StringBuilder gitBranchesSHAReportStringBuilder = new StringBuilder();

		gitBranchesSHAReportStringBuilder.append(
			_getLocalGitBranchesSHAReport());
		gitBranchesSHAReportStringBuilder.append("\n");
		gitBranchesSHAReportStringBuilder.append(
			_getRemoteGitBranchesSHAReport(null, remoteURL));

		StringBuilder sb = new StringBuilder();

		sb.append("git fetch --progress -v -f");

		if (noTags) {
			sb.append(" --no-tags");
		}
		else {
			sb.append(" --tags");
		}

		sb.append(" ");
		sb.append(remoteURL);
		sb.append(" refs/heads/*:refs/remotes/origin/*");

		long start = System.currentTimeMillis();

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			3, GitUtil.MILLIS_RETRY_DELAY, 1000 * 60 * 30, sb.toString());

		if (executionResult.getExitValue() != 0) {
			System.out.println(gitBranchesSHAReportStringBuilder.toString());

			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to fetch from remote url ", remoteURL, "\n",
					executionResult.getStandardError()));
		}

		long duration = System.currentTimeMillis() - start;

		System.out.println(
			"Fetch completed in " +
				JenkinsResultsParserUtil.toDurationString(duration));

		if (duration > (1000 * 60)) {
			System.out.println(gitBranchesSHAReportStringBuilder.toString());
		}
	}

	public LocalGitBranch fetch(
		String branchName, LocalGitBranch localGitBranch) {

		if (localGitBranch == null) {
			throw new IllegalArgumentException("Local Git branch is null");
		}

		StringBuilder sb = new StringBuilder();

		sb.append("git fetch --progress -v -f --no-tags ");
		sb.append(String.valueOf(localGitBranch.getDirectory()));
		sb.append(" ");
		sb.append(localGitBranch.getName());

		if ((branchName != null) && !branchName.isEmpty()) {
			sb.append(":");
			sb.append(branchName);
		}

		long start = System.currentTimeMillis();

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			3, GitUtil.MILLIS_RETRY_DELAY, 1000 * 60 * 30, sb.toString());

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to fetch from local Git repository ",
					String.valueOf(localGitBranch.getDirectory()), "\n",
					executionResult.getStandardError()));
		}

		String durationString = JenkinsResultsParserUtil.toDurationString(
			System.currentTimeMillis() - start);

		System.out.println("Fetch completed in " + durationString);

		return createLocalGitBranch(
			localGitBranch.getName(), true, localGitBranch.getSHA());
	}

	public void gc() {
		int retries = 0;

		while (true) {
			GitUtil.ExecutionResult executionResult = null;

			boolean exceptionThrown = false;

			try {
				executionResult = executeBashCommands(
					GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
					60 * 60 * 1000, "git gc");
			}
			catch (RuntimeException re) {
				exceptionThrown = true;
			}

			System.out.println(executionResult.getStandardOut());

			if (exceptionThrown || (executionResult.getExitValue() != 0)) {
				String standardError = executionResult.getStandardError();

				Matcher matcher = _badRefPattern.matcher(standardError);

				if (matcher.find()) {
					File badRefFile = new File(
						getWorkingDirectory(),
						".git/" + matcher.group("badRef"));

					badRefFile.delete();
				}

				if (retries > 1) {
					throw new RuntimeException(
						JenkinsResultsParserUtil.combine(
							"Unable to garbage collect Git\n", standardError));
				}
			}
			else {
				return;
			}

			retries++;

			JenkinsResultsParserUtil.sleep(GitUtil.MILLIS_RETRY_DELAY);

			System.out.println(
				JenkinsResultsParserUtil.combine(
					"Retry garbage collect Git in ",
					String.valueOf(GitUtil.MILLIS_RETRY_DELAY), "ms"));
		}
	}

	public List<String> getBranchNamesContainingSHA(String sha) {
		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY, 1000 * 60 * 2,
			"git branch --contains " + sha);

		if (executionResult.getExitValue() != 0) {
			String standardError = executionResult.getStandardError();

			if (standardError.contains("no such commit")) {
				return Collections.emptyList();
			}

			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to get branches with SHA ", sha, "\n",
					standardError));
		}

		String standardOut = executionResult.getStandardOut();

		if (standardOut.contains("no such commit")) {
			return Collections.emptyList();
		}

		String[] lines = standardOut.split("\n");

		List<String> branchNamesList = new ArrayList<>(lines.length - 1);

		for (String line : lines) {
			String branchName = line.trim();

			if (branchName.startsWith("* ")) {
				branchName = branchName.substring(2);
			}

			if (branchName.isEmpty()) {
				continue;
			}

			branchNamesList.add(branchName);
		}

		return branchNamesList;
	}

	public String getCurrentBranchName() {
		return getCurrentBranchName(false);
	}

	public String getCurrentBranchName(boolean required) {
		waitForIndexLock();

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT, "git branch | grep \\*");

		if (executionResult.getExitValue() != 0) {
			System.out.println(executionResult.getStandardError());

			if (required) {
				throw new RuntimeException(
					"Unable to find required local branch HEAD");
			}

			return null;
		}

		String currentBranchName = executionResult.getStandardOut();

		currentBranchName = currentBranchName.replaceFirst("\\*\\s*", "");

		currentBranchName = currentBranchName.trim();

		if (currentBranchName.isEmpty()) {
			return null;
		}

		return currentBranchName;
	}

	public String getGitConfigProperty(String gitConfigPropertyName) {
		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT, "git config " + gitConfigPropertyName);

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to read Git config property ",
					gitConfigPropertyName, "\n",
					executionResult.getStandardError()));
		}

		String configProperty = executionResult.getStandardOut();

		if (configProperty != null) {
			configProperty = configProperty.trim();
		}

		if ((configProperty == null) || configProperty.isEmpty()) {
			return null;
		}

		return configProperty;
	}

	public Boolean getGitConfigPropertyBoolean(
		String gitConfigPropertyName, Boolean defaultValue) {

		String gitConfigProperty = getGitConfigProperty(gitConfigPropertyName);

		if (gitConfigProperty == null) {
			if (defaultValue != null) {
				return defaultValue;
			}

			return null;
		}

		return Boolean.parseBoolean(gitConfigProperty);
	}

	public File getJavaFileFromFullClassName(String fullClassName) {
		if (_javaDirPaths == null) {
			List<File> javaFiles = JenkinsResultsParserUtil.findFiles(
				getWorkingDirectory(), ".*\\.java");

			_javaDirPaths = new HashSet<>();

			for (File javaFile : javaFiles) {
				File parentFile = javaFile.getParentFile();

				_javaDirPaths.add(parentFile.getPath());
			}
		}

		String classFileName =
			fullClassName.replaceAll(".*\\.([^\\.]+)", "$1") + ".java";

		String classPackageName = fullClassName.substring(
			0, fullClassName.lastIndexOf("."));

		String classPackagePath = classPackageName.replaceAll("\\.", "/");

		for (String javaDirPath : _javaDirPaths) {
			if (!javaDirPath.contains(classPackagePath)) {
				continue;
			}

			File classFile = new File(javaDirPath, classFileName);

			if (!classFile.exists()) {
				continue;
			}

			String classFilePath = classFile.getPath();

			if (!classFilePath.contains(
					classPackagePath + "/" + classFileName)) {

				continue;
			}

			return classFile;
		}

		return null;
	}

	public List<File> getModifiedDirsList(
		boolean checkUnstagedFiles, List<PathMatcher> excludesPathMatchers,
		List<PathMatcher> includesPathMatchers) {

		return getModifiedDirsList(
			checkUnstagedFiles, excludesPathMatchers, includesPathMatchers,
			getWorkingDirectory());
	}

	public List<File> getModifiedDirsList(
		boolean checkUnstagedFiles, List<PathMatcher> excludesPathMatchers,
		List<PathMatcher> includesPathMatchers, File rootDirectory) {

		List<File> subdirectories = getSubdirectoriesContainingFiles(
			1, getModifiedFilesList(checkUnstagedFiles, null, null),
			rootDirectory);

		return JenkinsResultsParserUtil.getIncludedFiles(
			excludesPathMatchers, includesPathMatchers, subdirectories);
	}

	public List<File> getModifiedFilesList() {
		return getModifiedFilesList(false, null, null);
	}

	public List<File> getModifiedFilesList(boolean checkUnstagedFiles) {
		return getModifiedFilesList(checkUnstagedFiles, null, null);
	}

	public List<File> getModifiedFilesList(
		boolean checkUnstagedFiles, List<PathMatcher> excludesPathMatchers,
		List<PathMatcher> includesPathMatchers) {

		LocalGitBranch currentLocalGitBranch = getCurrentLocalGitBranch();

		if (currentLocalGitBranch == null) {
			throw new RuntimeException(
				"Unable to determine the current branch");
		}

		StringBuilder sb = new StringBuilder();

		sb.append("git diff --diff-filter=ADMR --name-only ");

		sb.append(
			_getMergeBaseCommitSHA(
				currentLocalGitBranch,
				getLocalGitBranch(getUpstreamBranchName(), true)));

		if (!checkUnstagedFiles) {
			sb.append(" ");
			sb.append(currentLocalGitBranch.getSHA());
		}

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT, sb.toString());

		if (executionResult.getExitValue() == 1) {
			return Collections.emptyList();
		}

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				"Unable to get current branch modified files\n" +
					executionResult.getStandardError());
		}

		List<File> modifiedFiles = new ArrayList<>();

		String gitDiffOutput = executionResult.getStandardOut();

		for (String line : gitDiffOutput.split("\n")) {
			modifiedFiles.add(new File(_workingDirectory, line));
		}

		return JenkinsResultsParserUtil.getIncludedFiles(
			excludesPathMatchers, includesPathMatchers, modifiedFiles);
	}

	public List<File> getModifiedFilesList(
		List<PathMatcher> excludesPathMatchers,
		List<PathMatcher> includesPathMatchers) {

		return getModifiedFilesList(
			false, excludesPathMatchers, includesPathMatchers);
	}

	public LocalGitBranch getRebasedLocalGitBranch(PullRequest pullRequest) {
		return getRebasedLocalGitBranch(
			pullRequest.getLocalSenderBranchName(),
			pullRequest.getSenderBranchName(), pullRequest.getSenderRemoteURL(),
			pullRequest.getSenderSHA(), pullRequest.getUpstreamBranchName(),
			pullRequest.getLiferayRemoteBranchSHA());
	}

	public LocalGitBranch getRebasedLocalGitBranch(
		String rebasedLocalGitBranchName, String senderRefName,
		String senderRemoteURL, String senderSHA, String upstreamBranchName,
		String upstreamBranchSHA) {

		String currentBranchName = getCurrentBranchName();

		LocalGitBranch tempLocalGitBranch = null;

		try {
			if ((currentBranchName == null) ||
				currentBranchName.equals(rebasedLocalGitBranchName)) {

				tempLocalGitBranch = createLocalGitBranch(
					"temp-" + System.currentTimeMillis());

				checkoutLocalGitBranch(tempLocalGitBranch);
			}

			RemoteGitBranch senderRemoteGitBranch = getRemoteGitBranch(
				senderRefName, senderRemoteURL, true);

			fetch(senderRemoteGitBranch);

			LocalGitBranch rebasedLocalGitBranch = createLocalGitBranch(
				rebasedLocalGitBranchName, true, senderSHA);

			RemoteGitBranch upstreamRemoteGitBranch = getRemoteGitBranch(
				upstreamBranchName, getUpstreamGitRemote(), true);

			if (upstreamBranchSHA == null) {
				upstreamBranchSHA = upstreamRemoteGitBranch.getSHA();
			}

			if (!localSHAExists(upstreamBranchSHA)) {
				fetch(upstreamRemoteGitBranch);
			}

			LocalGitBranch upstreamLocalGitBranch = createLocalGitBranch(
				upstreamRemoteGitBranch.getName(), true, upstreamBranchSHA);

			rebasedLocalGitBranch = rebase(
				true, upstreamLocalGitBranch, rebasedLocalGitBranch);

			clean();

			reset("--hard");

			return rebasedLocalGitBranch;
		}
		finally {
			if (tempLocalGitBranch != null) {
				deleteLocalGitBranch(tempLocalGitBranch);
			}
		}
	}

	public RemoteGitBranch getRemoteGitBranch(
		String remoteGitBranchName, GitRemote gitRemote) {

		return getRemoteGitBranch(
			remoteGitBranchName, gitRemote.getRemoteURL(), false);
	}

	public RemoteGitBranch getRemoteGitBranch(
		String remoteGitBranchName, GitRemote gitRemote, boolean required) {

		return getRemoteGitBranch(
			remoteGitBranchName, gitRemote.getRemoteURL(), required);
	}

	public RemoteGitBranch getRemoteGitBranch(
		String remoteGitBranchName, RemoteGitRepository remoteGitRepository) {

		return getRemoteGitBranch(
			remoteGitBranchName, remoteGitRepository.getRemoteURL(), false);
	}

	public RemoteGitBranch getRemoteGitBranch(
		String remoteGitBranchName, RemoteGitRepository remoteGitRepository,
		boolean required) {

		return getRemoteGitBranch(
			remoteGitBranchName, remoteGitRepository.getRemoteURL(), required);
	}

	public RemoteGitBranch getRemoteGitBranch(
		String remoteGitBranchName, String remoteURL) {

		return getRemoteGitBranch(remoteGitBranchName, remoteURL, false);
	}

	public RemoteGitBranch getRemoteGitBranch(
		String remoteGitBranchName, String remoteURL, boolean required) {

		List<RemoteGitBranch> remoteGitBranches = getRemoteGitBranches(
			remoteGitBranchName, remoteURL);

		for (RemoteGitBranch remoteGitBranch : remoteGitBranches) {
			if (remoteGitBranchName.equals(remoteGitBranch.getName())) {
				return remoteGitBranch;
			}
		}

		if (required) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to find required branch ", remoteGitBranchName,
					" from remote URL ", remoteURL));
		}

		return null;
	}

	public List<RemoteGitBranch> getRemoteGitBranches(GitRemote gitRemote) {
		return getRemoteGitBranches(null, gitRemote.getRemoteURL());
	}

	public List<RemoteGitBranch> getRemoteGitBranches(
		RemoteGitRepository remoteGitRepository) {

		return getRemoteGitBranches(null, remoteGitRepository.getRemoteURL());
	}

	public List<RemoteGitBranch> getRemoteGitBranches(String remoteURL) {
		return getRemoteGitBranches(null, remoteURL);
	}

	public List<RemoteGitBranch> getRemoteGitBranches(
		String remoteGitBranchName, GitRemote gitRemote) {

		return getRemoteGitBranches(
			remoteGitBranchName, gitRemote.getRemoteURL());
	}

	public List<RemoteGitBranch> getRemoteGitBranches(
		String remoteGitBranchName, RemoteGitRepository remoteGitRepository) {

		return getRemoteGitBranches(
			remoteGitBranchName, remoteGitRepository.getRemoteURL());
	}

	public List<RemoteGitBranch> getRemoteGitBranches(
		String remoteGitBranchName, String remoteURL) {

		return GitUtil.getRemoteGitBranches(
			remoteGitBranchName, _workingDirectory, remoteURL);
	}

	public List<String> getRemoteGitBranchNames(GitRemote gitRemote) {
		return getRemoteGitBranchNames(gitRemote.getRemoteURL());
	}

	public List<String> getRemoteGitBranchNames(
		RemoteGitRepository remoteGitRepository) {

		return getRemoteGitBranchNames(remoteGitRepository.getRemoteURL());
	}

	public List<String> getRemoteGitBranchNames(String remoteURL) {
		List<String> remoteGitBranchNames = new ArrayList<>();

		List<RemoteGitBranch> remoteGitBranches = getRemoteGitBranches(
			remoteURL);

		for (RemoteGitBranch remoteGitBranch : remoteGitBranches) {
			remoteGitBranchNames.add(remoteGitBranch.getName());
		}

		return remoteGitBranchNames;
	}

	public String getRemoteGitBranchSHA(
		String remoteGitBranchName, GitRemote gitRemote) {

		return getRemoteGitBranchSHA(
			remoteGitBranchName, gitRemote.getRemoteURL());
	}

	public String getRemoteGitBranchSHA(
		String remoteGitBranchName, RemoteGitRepository remoteGitRepository) {

		return getRemoteGitBranchSHA(
			remoteGitBranchName, remoteGitRepository.getRemoteURL());
	}

	public String getRemoteGitBranchSHA(
		String remoteGitBranchName, String remoteURL) {

		if (remoteGitBranchName == null) {
			throw new IllegalArgumentException("Remote branch name is null");
		}

		if (remoteURL == null) {
			throw new IllegalArgumentException("Remote URL is null");
		}

		if (!GitUtil.isValidRemoteURL(remoteURL)) {
			throw new IllegalArgumentException(
				"Invalid remote url " + remoteURL);
		}

		String command = JenkinsResultsParserUtil.combine(
			"git ls-remote -h ", remoteURL, " ", remoteGitBranchName);

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			1000 * 60 * 10, command);

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to get remote branch SHA ", remoteURL, " ",
					remoteGitBranchName, "\n",
					executionResult.getStandardError()));
		}

		String input = executionResult.getStandardOut();

		for (String line : input.split("\n")) {
			Matcher matcher = GitRemote.gitLsRemotePattern.matcher(line);

			if (matcher.find()) {
				return matcher.group("sha");
			}
		}

		return null;
	}

	public RemoteGitRef getRemoteGitRef(
		String remoteGitRefName, String remoteURL, boolean required) {

		List<RemoteGitRef> remoteGitRefs = getRemoteGitRefs(
			remoteGitRefName, remoteURL);

		for (RemoteGitRef remoteGitRef : remoteGitRefs) {
			if (remoteGitRefName.equals(remoteGitRef.getName())) {
				return remoteGitRef;
			}
		}

		if (required) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to find required git ref ", remoteGitRefName,
					" from remote URL ", remoteURL));
		}

		return null;
	}

	public List<RemoteGitRef> getRemoteGitRefs(
		String remoteGitRefName, String remoteURL) {

		return GitUtil.getRemoteGitRefs(
			remoteGitRefName, _workingDirectory, remoteURL);
	}

	public String getUpstreamBranchName() {
		return _upstreamBranchName;
	}

	public LocalGitBranch getUpstreamLocalGitBranch() {
		String upstreamBranchName = getUpstreamBranchName();

		if (localGitBranchExists(upstreamBranchName)) {
			return _getLocalGitBranch(upstreamBranchName, true);
		}

		RemoteGitBranch upstreamRemoteGitBranch = getRemoteGitBranch(
			upstreamBranchName, getGitRemote("upstream"));

		fetch(upstreamRemoteGitBranch);

		String currentBranchName = getCurrentBranchName();

		if (currentBranchName == null) {
			List<String> localGitBranchNames = getLocalGitBranchNames();

			List<LocalGitBranch> localGitBranches = getLocalGitBranches(
				localGitBranchNames.get(0));

			checkoutLocalGitBranch(localGitBranches.get(0));
		}

		return createLocalGitBranch(
			upstreamBranchName, true, upstreamRemoteGitBranch.getSHA());
	}

	public RemoteGitBranch getUpstreamRemoteGitBranch() {
		return getRemoteGitBranch(
			getUpstreamBranchName(),
			JenkinsResultsParserUtil.combine(
				"git@github.com:liferay/", getGitRepositoryName()));
	}

	public File getWorkingDirectory() {
		return _workingDirectory;
	}

	public boolean isRemoteGitRepositoryAlive(String remoteURL) {
		String command = JenkinsResultsParserUtil.combine(
			"git ls-remote -h ", remoteURL, " HEAD");

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			1000 * 60 * 10, command);

		if (executionResult.getExitValue() != 0) {
			System.out.println("Unable to connect to " + remoteURL);

			return false;
		}

		System.out.println(remoteURL + " is alive");

		return true;
	}

	public boolean localGitBranchExists(String branchName) {
		waitForIndexLock();

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT,
			"git branch | grep [\\s\\*]*" + branchName + "$");

		if (executionResult.getExitValue() == 0) {
			String standardOut = executionResult.getStandardOut();

			if (standardOut.isEmpty()) {
				return false;
			}

			return true;
		}

		return false;
	}

	public boolean localSHAExists(String sha) {
		String command = "git cat-file -t " + sha;

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY, 1000 * 60 * 3,
			command);

		if (executionResult.getExitValue() == 0) {
			return true;
		}

		return false;
	}

	public List<LocalGitCommit> log(int num) {
		return _log(0, num, null, null);
	}

	public List<LocalGitCommit> log(int num, File file) {
		return _log(0, num, file, null);
	}

	public List<LocalGitCommit> log(int start, int num) {
		return _log(start, num, null, null);
	}

	public List<LocalGitCommit> log(int start, int num, String sha) {
		return _log(start, num, null, sha);
	}

	public RemoteGitBranch pushToRemoteGitRepository(
		boolean force, LocalGitBranch localGitBranch,
		String remoteGitBranchName, GitRemote gitRemote) {

		return pushToRemoteGitRepository(
			force, localGitBranch, remoteGitBranchName,
			gitRemote.getRemoteURL());
	}

	public RemoteGitBranch pushToRemoteGitRepository(
		boolean force, LocalGitBranch localGitBranch,
		String remoteGitBranchName, RemoteGitRepository remoteGitRepository) {

		return pushToRemoteGitRepository(
			force, localGitBranch, remoteGitBranchName,
			remoteGitRepository.getRemoteURL());
	}

	public RemoteGitBranch pushToRemoteGitRepository(
		boolean force, LocalGitBranch localGitBranch,
		String remoteGitBranchName, String remoteURL) {

		if (localGitBranch == null) {
			throw new IllegalArgumentException("Local Git branch is null");
		}

		if (remoteURL == null) {
			throw new IllegalArgumentException("Remote URL is null");
		}

		if (!GitUtil.isValidRemoteURL(remoteURL)) {
			throw new IllegalArgumentException(
				"Invalid remote url " + remoteURL);
		}

		StringBuilder sb = new StringBuilder();

		sb.append("git push ");

		if (force) {
			sb.append("-f ");
		}

		sb.append(remoteURL);
		sb.append(" ");
		sb.append(localGitBranch.getName());

		if (remoteGitBranchName != null) {
			sb.append(":");
			sb.append(remoteGitBranchName);
		}

		try {
			GitUtil.ExecutionResult executionResult = executeBashCommands(
				GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
				1000 * 60 * 10, sb.toString());

			if (executionResult.getExitValue() != 0) {
				return null;
			}
		}
		catch (RuntimeException re) {
			re.printStackTrace();

			return null;
		}

		return (RemoteGitBranch)GitBranchFactory.newRemoteGitRef(
			GitRepositoryFactory.getRemoteGitRepository(remoteURL),
			remoteGitBranchName, localGitBranch.getSHA(), "heads");
	}

	public LocalGitBranch rebase(
		boolean abortOnFail, LocalGitBranch baseLocalGitBranch,
		LocalGitBranch localGitBranch) {

		List<String> branchNamesContainingSHA = getBranchNamesContainingSHA(
			baseLocalGitBranch.getSHA());

		if (branchNamesContainingSHA.contains(localGitBranch.getName())) {
			checkoutLocalGitBranch(localGitBranch);

			return localGitBranch;
		}

		checkoutLocalGitBranch(baseLocalGitBranch);

		reset("--hard " + baseLocalGitBranch.getSHA());

		String rebaseCommand = JenkinsResultsParserUtil.combine(
			"git rebase ", baseLocalGitBranch.getName(), " ",
			localGitBranch.getName());

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			1000 * 60 * 10, rebaseCommand);

		if (executionResult.getExitValue() != 0) {
			if (abortOnFail) {
				rebaseAbort();
			}

			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to rebase ", localGitBranch.getName(), " to ",
					baseLocalGitBranch.getName(), "\n",
					executionResult.getStandardError()));
		}

		return getCurrentLocalGitBranch();
	}

	public void rebaseAbort() {
		rebaseAbort(true);
	}

	public void rebaseAbort(boolean ignoreFailure) {
		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT, "git rebase --abort");

		if (!ignoreFailure && (executionResult.getExitValue() != 0)) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to abort rebase\n",
					executionResult.getStandardError()));
		}
	}

	public boolean remoteGitBranchExists(
		String branchName, GitRemote gitRemote) {

		return remoteGitBranchExists(branchName, gitRemote.getRemoteURL());
	}

	public boolean remoteGitBranchExists(
		String branchName, RemoteGitRepository remoteGitRepository) {

		return remoteGitBranchExists(
			branchName, remoteGitRepository.getRemoteURL());
	}

	public boolean remoteGitBranchExists(String branchName, String remoteURL) {
		if (getRemoteGitBranch(branchName, remoteURL) != null) {
			return true;
		}

		return false;
	}

	public void reset(String options) {
		String command = "git reset " + options;

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			2, GitUtil.MILLIS_RETRY_DELAY, 1000 * 60 * 5, command);

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to reset\n", executionResult.getStandardError()));
		}
	}

	public void stageFileInCurrentLocalGitBranch(String fileName) {
		String command = "git stage " + fileName;

		GitUtil.ExecutionResult result = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT, command);

		if (result.getExitValue() != 0) {
			throw new RuntimeException("Unable to stage file " + fileName);
		}
	}

	public String status() {
		for (int i = 0; i < 5; i++) {
			try {
				String gitStatus = _status();

				gitStatus = gitStatus.replaceAll(
					"Finished executing Bash commands.", "");

				if (!gitStatus.startsWith("On branch")) {
					throw new RuntimeException("Unable to run: git status");
				}

				return gitStatus;
			}
			catch (RuntimeException re) {
				re.printStackTrace();

				JenkinsResultsParserUtil.sleep(1000);
			}
		}

		throw new RuntimeException("Unable to run: git status");
	}

	protected GitWorkingDirectory(
			String upstreamBranchName, String workingDirectoryPath)
		throws IOException {

		this(upstreamBranchName, workingDirectoryPath, null);
	}

	protected GitWorkingDirectory(
			String upstreamBranchName, String workingDirectoryPath,
			String gitRepositoryName)
		throws IOException {

		setWorkingDirectory(workingDirectoryPath);

		_upstreamBranchName = upstreamBranchName;

		GitRemote upstreamTempGitRemote = getGitRemote("upstream-temp");

		if (upstreamTempGitRemote != null) {
			removeGitRemote(upstreamTempGitRemote);
		}

		waitForIndexLock();

		if ((gitRepositoryName == null) || gitRepositoryName.equals("")) {
			gitRepositoryName = loadGitRepositoryName();
		}

		_gitRepositoryName = gitRepositoryName;

		if (_publicOnlyGitRepositoryNames.contains(_gitRepositoryName)) {
			setUpstreamGitRemoteToPublicGitRepository();
		}
		else {
			if (_privateOnlyGitRepositoryNames.contains(_gitRepositoryName)) {
				setUpstreamGitRemoteToPrivateGitRepository();
			}
			else {
				if (upstreamBranchName.equals("master")) {
					setUpstreamGitRemoteToPublicGitRepository();
				}
				else {
					setUpstreamGitRemoteToPrivateGitRepository();
				}
			}
		}

		_gitRepositoryUsername = loadGitRepositoryUsername();
	}

	protected GitUtil.ExecutionResult executeBashCommands(
		int maxRetries, long retryDelay, long timeout, String... commands) {

		return GitUtil.executeBashCommands(
			maxRetries, retryDelay, timeout, _workingDirectory, commands);
	}

	protected Map<String, String> getLocalGitBranchesShaMap() {
		File workingDirectory = getWorkingDirectory();

		String command = JenkinsResultsParserUtil.combine(
			"git ls-remote -h ",
			JenkinsResultsParserUtil.getCanonicalPath(workingDirectory));

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			1000 * 60 * 10, command);

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to get local Git branch SHAs\n",
					executionResult.getStandardError()));
		}

		String input = executionResult.getStandardOut();

		String[] inputLines = input.split("\n");

		Map<String, String> localGitBranchesShaMap = new HashMap<>(
			inputLines.length);

		for (String line : inputLines) {
			Matcher matcher = GitRemote.gitLsRemotePattern.matcher(line);

			if (matcher.find()) {
				localGitBranchesShaMap.put(
					matcher.group("name"), matcher.group("sha"));
			}
		}

		return localGitBranchesShaMap;
	}

	protected List<String> getLocalGitBranchNames() {
		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT,
			"git for-each-ref refs/heads --format=\"%(refname)\"");

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to get local branch names\n",
					executionResult.getStandardError()));
		}

		String standardOut = executionResult.getStandardOut();

		return toShortNameList(Arrays.asList(standardOut.split("\n")));
	}

	protected LocalGitCommit getLocalGitCommit(String gitLogEntity) {
		Matcher matcher = _gitLogEntityPattern.matcher(gitLogEntity);

		if (!matcher.matches()) {
			throw new IllegalArgumentException("Unable to find Git SHA");
		}

		int unixTimestamp = Integer.valueOf(matcher.group("commitTime"));

		long epochTimestamp = (long)unixTimestamp * 1000;

		return GitCommitFactory.newLocalGitCommit(
			this, matcher.group("message"), matcher.group("sha"),
			epochTimestamp);
	}

	protected List<File> getSubdirectoriesContainingFiles(
		int depth, List<File> files, File rootDirectory) {

		List<File> subdirectories = JenkinsResultsParserUtil.getSubdirectories(
			depth, rootDirectory);

		return JenkinsResultsParserUtil.getDirectoriesContainingFiles(
			subdirectories, files);
	}

	protected String loadGitRepositoryName() {
		GitRemote upstreamGitRemote = getUpstreamGitRemote();

		String remoteURL = upstreamGitRemote.getRemoteURL();

		int x = remoteURL.lastIndexOf("/") + 1;

		int y = remoteURL.indexOf(".git");

		if (y == -1) {
			y = remoteURL.length();
		}

		String gitRepositoryName = remoteURL.substring(x, y);

		if (gitRepositoryName.equals("liferay-jenkins-tools-private")) {
			return gitRepositoryName;
		}

		if ((gitRepositoryName.equals("liferay-plugins-ee") ||
			 gitRepositoryName.equals("liferay-portal-ee")) &&
			_upstreamBranchName.equals("master")) {

			gitRepositoryName = gitRepositoryName.replace("-ee", "");
		}

		if (gitRepositoryName.contains("-private") &&
			!_upstreamBranchName.contains("-private")) {

			gitRepositoryName = gitRepositoryName.replace("-private", "");
		}

		return gitRepositoryName;
	}

	protected String loadGitRepositoryUsername() {
		GitRemote upstreamGitRemote = getUpstreamGitRemote();

		String remoteURL = upstreamGitRemote.getRemoteURL();

		int x = remoteURL.indexOf(":") + 1;
		int y = remoteURL.indexOf("/");

		return remoteURL.substring(x, y);
	}

	protected void setUpstreamGitRemoteToPrivateGitRepository() {
		GitRemote gitRemote = getUpstreamGitRemote();

		String privateGitRepositoryName = GitUtil.getPrivateRepositoryName(
			getGitRepositoryName());

		RemoteGitRepository remoteGitRepository =
			GitRepositoryFactory.getRemoteGitRepository(
				"github.com", privateGitRepositoryName,
				gitRemote.getUsername());

		addGitRemote(true, "upstream-temp", remoteGitRepository.getRemoteURL());
	}

	protected void setUpstreamGitRemoteToPublicGitRepository() {
		GitRemote gitRemote = getUpstreamGitRemote();

		String publicGitRepositoryName = GitUtil.getPublicRepositoryName(
			getGitRepositoryName());

		RemoteGitRepository remoteGitRepository =
			GitRepositoryFactory.getRemoteGitRepository(
				"github.com", publicGitRepositoryName, gitRemote.getUsername());

		addGitRemote(true, "upstream-temp", remoteGitRepository.getRemoteURL());
	}

	protected void setWorkingDirectory(String workingDirectoryPath)
		throws IOException {

		_workingDirectory = new File(workingDirectoryPath);

		if (!_workingDirectory.exists()) {
			throw new FileNotFoundException(
				_workingDirectory.getPath() + " is unavailable");
		}

		_gitDirectory = new File(workingDirectoryPath, ".git");

		if (_gitDirectory.isFile()) {
			_gitDirectory = getRealGitDirectory(_gitDirectory);
		}

		if (!_gitDirectory.exists()) {
			throw new FileNotFoundException(
				_gitDirectory.getPath() + " is unavailable");
		}
	}

	protected List<String> toShortNameList(List<String> fullNameList) {
		List<String> shortNames = new ArrayList<>(fullNameList.size());

		for (String fullName : fullNameList) {
			shortNames.add(fullName.substring("refs/heads/".length()));
		}

		return shortNames;
	}

	protected void waitForIndexLock() {
		int retries = 0;

		File file = new File(_gitDirectory, "index.lock");

		while (file.exists()) {
			System.out.println("Waiting for index.lock to be cleared.");

			JenkinsResultsParserUtil.sleep(5000);

			retries++;

			if (retries >= 24) {
				file.delete();
			}
		}
	}

	private static List<String> _getBuildPropertyAsList(String key) {
		try {
			return JenkinsResultsParserUtil.getBuildPropertyAsList(true, key);
		}
		catch (IOException ioe) {
			throw new RuntimeException(
				"Unable to get build property " + key, ioe);
		}
	}

	private boolean _deleteLocalGitBranches(String... branchNames) {
		StringBuilder sb = new StringBuilder();

		sb.append("git branch -D -f ");

		String joinedBranchNames = JenkinsResultsParserUtil.join(
			" ", branchNames);

		sb.append(joinedBranchNames);

		GitUtil.ExecutionResult executionResult = null;

		boolean exceptionThrown = false;

		try {
			executionResult = executeBashCommands(
				GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
				1000 * 60 * 10, sb.toString());
		}
		catch (RuntimeException re) {
			exceptionThrown = true;
		}

		if (exceptionThrown || (executionResult.getExitValue() != 0)) {
			System.out.println(
				JenkinsResultsParserUtil.combine(
					"Unable to delete local branches:", "\n    ",
					joinedBranchNames.replaceAll("\\s", "\n    "), "\n",
					executionResult.getStandardError()));

			return false;
		}

		System.out.println(
			JenkinsResultsParserUtil.combine(
				"Deleted local branches:", "\n    ",
				joinedBranchNames.replaceAll("\\s", "\n    ")));

		return true;
	}

	private boolean _deleteRemoteGitBranches(
		String remoteURL, String... branchNames) {

		StringBuilder sb = new StringBuilder();

		sb.append("git push --delete ");
		sb.append(remoteURL);
		sb.append(" ");

		String joinedBranchNames = JenkinsResultsParserUtil.join(
			" ", branchNames);

		sb.append(joinedBranchNames);

		GitUtil.ExecutionResult executionResult = null;

		boolean exceptionThrown = false;

		try {
			executionResult = executeBashCommands(
				GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
				1000 * 60 * 10, sb.toString());
		}
		catch (RuntimeException re) {
			exceptionThrown = true;
		}

		if (exceptionThrown || (executionResult.getExitValue() != 0)) {
			System.out.println(
				JenkinsResultsParserUtil.combine(
					"Unable to delete ", remoteURL, " branches:\n    ",
					joinedBranchNames.replaceAll("\\s", "\n    "), "\n",
					executionResult.getStandardError()));

			return false;
		}

		System.out.println(
			JenkinsResultsParserUtil.combine(
				"Deleted ", remoteURL, " branches:", "\n    ",
				joinedBranchNames.replaceAll("\\s", "\n    ")));

		return true;
	}

	private LocalGitBranch _getLocalGitBranch(
		String branchName, boolean required) {

		List<LocalGitBranch> localGitBranches = getLocalGitBranches(branchName);

		if ((localGitBranches != null) && !localGitBranches.isEmpty()) {
			return localGitBranches.get(0);
		}

		if (required) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to find required branch ", branchName, " from ",
					String.valueOf(getWorkingDirectory())));
		}

		return null;
	}

	private String _getLocalGitBranchesSHAReport() {
		StringBuilder sb = new StringBuilder("Local Git branches");

		for (LocalGitBranch localGitBranch : getLocalGitBranches(null)) {
			sb.append("\n    ");

			sb.append(localGitBranch.getName());
			sb.append(": ");
			sb.append(localGitBranch.getSHA());
		}

		return sb.toString();
	}

	private String _getMergeBaseCommitSHA(LocalGitBranch... localGitBranches) {
		if (localGitBranches.length < 2) {
			throw new IllegalArgumentException(
				"Unable to perform merge-base with less than two branches");
		}

		StringBuilder sb = new StringBuilder("git merge-base");

		for (LocalGitBranch localGitBranch : localGitBranches) {
			sb.append(" ");
			sb.append(localGitBranch.getName());
		}

		GitUtil.ExecutionResult executionResult = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT, sb.toString());

		if (executionResult.getExitValue() != 0) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					"Unable to get merge base commit SHA\n",
					executionResult.getStandardError()));
		}

		return executionResult.getStandardOut();
	}

	private String _getRemoteGitBranchesSHAReport(
		String remoteGitBranchName, String remoteURL) {

		StringBuilder sb = new StringBuilder("Remote Git branches");

		for (RemoteGitBranch remoteGitBranch :
				getRemoteGitBranches(remoteGitBranchName, remoteURL)) {

			sb.append("\n    ");

			sb.append(remoteGitBranch.getName());
			sb.append(": ");
			sb.append(remoteGitBranch.getSHA());
		}

		return sb.toString();
	}

	private List<LocalGitCommit> _log(
		int start, int num, File file, String sha) {

		List<LocalGitCommit> localGitCommits = new ArrayList<>(num);

		String gitLog = _log(start, num, file, "%H %ct %s", sha);

		gitLog = gitLog.replaceAll("Finished executing Bash commands.", "");

		String[] gitLogEntities = gitLog.split("\n");

		for (String gitLogEntity : gitLogEntities) {
			localGitCommits.add(getLocalGitCommit(gitLogEntity));
		}

		return localGitCommits;
	}

	private String _log(
		int start, int num, File file, String format, String sha) {

		if ((sha == null) || sha.isEmpty()) {
			sha = "HEAD";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("git log ");

		if (file != null) {
			sb.append("-n ");
			sb.append(num);
			sb.append(" ");
		}
		else {
			sb.append(sha);
			sb.append("~");
			sb.append(start + num);
			sb.append("..");
			sb.append(sha);
			sb.append("~");
			sb.append(start);
		}

		sb.append(" --pretty=format:'");
		sb.append(format);
		sb.append("'");

		if (file != null) {
			sb.append(" ");
			sb.append(JenkinsResultsParserUtil.getCanonicalPath(file));
		}

		GitUtil.ExecutionResult result = executeBashCommands(
			5, 1000, 30 * 1000, sb.toString());

		if (result.getExitValue() != 0) {
			throw new RuntimeException("Unable to run: git log");
		}

		return result.getStandardOut();
	}

	private String _status() {
		String command = "git status";

		GitUtil.ExecutionResult result = executeBashCommands(
			GitUtil.RETRIES_SIZE_MAX, GitUtil.MILLIS_RETRY_DELAY,
			GitUtil.MILLIS_TIMEOUT, command);

		if (result.getExitValue() != 0) {
			throw new RuntimeException("Unable to run: git status");
		}

		return result.getStandardOut();
	}

	private static final int _BRANCHES_DELETE_BATCH_SIZE = 5;

	private static final Pattern _badRefPattern = Pattern.compile(
		"fatal: bad object (?<badRef>.+/HEAD)");
	private static final Pattern _gitDirectoryPathPattern = Pattern.compile(
		"gitdir\\: (.*)\\s*");
	private static final Pattern _gitLogEntityPattern = Pattern.compile(
		"(?<sha>[0-9a-f]{40}) (?<commitTime>\\d+) (?<message>.*)");
	private static final List<String> _privateOnlyGitRepositoryNames =
		_getBuildPropertyAsList(
			"git.working.directory.private.only.repository.names");
	private static final List<String> _publicOnlyGitRepositoryNames =
		_getBuildPropertyAsList(
			"git.working.directory.public.only.repository.names");

	private File _gitDirectory;
	private final Map<String, GitRemote> _gitRemotes = new HashMap<>();
	private final String _gitRepositoryName;
	private final String _gitRepositoryUsername;
	private Set<String> _javaDirPaths;
	private final String _upstreamBranchName;
	private File _workingDirectory;

}