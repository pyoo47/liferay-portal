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

import org.dom4j.Element;

/**
 * @author Yi-Chen Tsai
 */
public class GitHubMessageTester {

	public static void main(String[] args) throws IOException {
/* Build topLevelBuild = BuildFactory.newBuild(
				"https://test-1-9.liferay.com/job/test-portal-acceptance-upstream-batch(7.0.x-private)/218/",
				null);
*/

		Build topLevelBuild = BuildFactory.newBuild(
				"https://test-1-18.liferay.com/job/test-portal-acceptance-pullrequest(ee-7.0.x)/467/",
				null);

		printGitHubMessage(topLevelBuild);

		//archiveBuild(topLevelBuild);
	}

	private static void archiveBuild(Build topLevelBuild) {
		topLevelBuild.archive(
			"test-portal-acceptance-pullrequest(master)_portal_startup_fail_unresolved_req");
	}

	private static void printGitHubMessage(Build topLevelBuild) {
		Element message = topLevelBuild.getGitHubMessageElement();

		try {
			System.out.println(Dom4JUtil.format(message, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}