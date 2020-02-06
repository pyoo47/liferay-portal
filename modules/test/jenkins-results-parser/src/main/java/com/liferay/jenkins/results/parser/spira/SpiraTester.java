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

package com.liferay.jenkins.results.parser.spira;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil.HttpRequestMethod;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Hashimoto
 */
public class SpiraTester {

	public static void main(String[] args) throws Exception {
		SpiraProject spiraProject = SpiraProject.getSpiraProjectById(12);

		System.out.println(
			SpiraRelease.createSpiraReleaseByPath(
				spiraProject,
				"/Sample #4/7.3.x/Pull Request/[master] ci:test:smoke"));

		SpiraRelease.createSpiraReleaseByPath(
			spiraProject, "/Sample #5/7.3.x/Upstream");

		System.out.println(
			spiraProject.getSpiraReleasesByPath("/Sample #5/7.3.x"));

		SpiraRelease.deleteSpiraReleasesByPath(
			spiraProject, "/Sample #4/7.1.x");

		//		spiraProject.getSpiraReleaseByPath("/invalid");
		//		spiraProject.getSpiraReleaseByPath("/Sample #4/7.1.x");

		//		request();
	}

	public static void request() throws IOException {
		String urlPath = "projects/{project_id}";

		Map<String, String> urlParameters = new HashMap<>();

		Map<String, String> urlPathReplacements = new HashMap<>();

		urlPathReplacements.put("project_id", "12");

		HttpRequestMethod httpRequestMethod = HttpRequestMethod.GET;

		String requestData = null;

		System.out.println(
			SpiraRestAPIUtil.request(
				urlPath, urlParameters, urlPathReplacements, httpRequestMethod,
				requestData));
	}

}