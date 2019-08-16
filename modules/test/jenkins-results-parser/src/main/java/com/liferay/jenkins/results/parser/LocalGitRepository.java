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

import java.io.File;

import java.util.List;
import java.util.Map;

/**
 * @author Peter Yoo
 */
public interface LocalGitRepository extends GitRepository {

	public static final Integer COMMITS_HISTORY_GROUP_SIZE = 100;

	public static final Integer COMMITS_HISTORY_SIZE_MAX = 25000;

	public GitRemote addGitRemote(
		boolean force, String gitRemoteName, String remoteURL, boolean write);

	public void checkout(LocalGitBranch localGitBranch, String options);

	public LocalGitBranch checkoutUpstreamLocalGitBranch();

	public LocalGitBranch createLocalGitBranch(
		String localGitBranchName, boolean force);

	public LocalGitBranch createLocalGitBranch(
		String localGitBranchName, boolean force, String startPoint);

	public boolean deleteLocalGitBranch(LocalGitBranch localGitBranch);

	public boolean deleteLocalGitBranches(
		List<LocalGitBranch> localGitBranches);

	public LocalGitBranch getCurrentLocalGitBranch();

	public File getDirectory();

	public File getDotGitDirectory();

	public GitRemote getGitRemote(String gitRemoteName);

	public GitWorkingDirectory getGitWorkingDirectory();

	public LocalGitBranch getLocalGitBranch(String localGitBranchName);

	public Map<String, LocalGitBranch> getLocalGitBranches();

	public List<LocalGitCommit> getRangeLocalGitCommits(
		String earliestSHA, String latestSHA);

	public String getUpstreamBranchName();

	public GitRemote getUpstreamGitRemote();

	public boolean gitRemoteExists(String gitRemoteName);

	public void removeGitRemote(GitRemote gitRemote, boolean write);

	public void removeGitRemote(String gitRemoteName, boolean write);

	public void removeGitRemotes(List<GitRemote> gitRemotes, boolean write);

	public void waitForIndexLock();

}