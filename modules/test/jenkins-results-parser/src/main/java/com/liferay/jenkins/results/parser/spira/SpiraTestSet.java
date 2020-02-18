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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class SpiraTestSet extends PathSpiraArtifact {

	public static SpiraTestSet createSpiraTestSet(
			SpiraProject spiraProject, String testSetName)
		throws IOException {

		return createSpiraTestSet(spiraProject, testSetName, null);
	}

	public static SpiraTestSet createSpiraTestSet(
			SpiraProject spiraProject, String testSetName,
			Integer parentTestSetFolderID)
		throws IOException {

		String testSetPath = "/" + testSetName;

		if (parentTestSetFolderID != null) {
			SpiraTestSetFolder parentSpiraTestSetFolder =
				spiraProject.getSpiraTestSetFolderByID(parentTestSetFolderID);

			testSetPath =
				parentSpiraTestSetFolder.getPath() + "/" + testSetName;
		}

		List<SpiraTestSet> spiraTestSets = spiraProject.getSpiraTestSetsByPath(
			testSetPath);

		if (!spiraTestSets.isEmpty()) {
			return spiraTestSets.get(0);
		}

		String urlPath = "projects/{project_id}/test-sets";

		Map<String, String> urlPathReplacements = new HashMap<>();

		urlPathReplacements.put(
			"project_id", String.valueOf(spiraProject.getID()));

		JSONObject requestJSONObject = new JSONObject();

		requestJSONObject.put(
			"Name", StringEscapeUtils.unescapeJava(testSetName));
		requestJSONObject.put("TestRunTypeId", TEST_RUN_TYPE_AUTOMATED);
		requestJSONObject.put("TestSetFolderId", parentTestSetFolderID);
		requestJSONObject.put("TestSetStatusId", STATUS_NOT_STARTED);

		JSONObject responseJSONObject = SpiraRestAPIUtil.requestJSONObject(
			urlPath, null, urlPathReplacements, HttpRequestMethod.POST,
			requestJSONObject.toString());

		SpiraTestSet spiraTestSet = spiraProject.getSpiraTestSetByID(
			responseJSONObject.getInt("TestSetId"));

		_spiraTestSets.put(
			_createSpiraTestSetKey(spiraProject.getID(), spiraTestSet.getID()),
			spiraTestSet);

		return spiraTestSet;
	}

	public static SpiraTestSet createSpiraTestSetByPath(
			SpiraProject spiraProject, String testSetPath)
		throws IOException {

		List<SpiraTestSet> spiraTestSets = spiraProject.getSpiraTestSetsByPath(
			testSetPath);

		if (!spiraTestSets.isEmpty()) {
			return spiraTestSets.get(0);
		}

		String testSetName = getPathName(testSetPath);
		String parentTestSetFolderPath = getParentPath(testSetPath);

		if (parentTestSetFolderPath.isEmpty()) {
			return createSpiraTestSet(spiraProject, testSetName);
		}

		SpiraTestSetFolder parentSpiraTestSetFolder =
			SpiraTestSetFolder.createSpiraTestSetFolderByPath(
				spiraProject, parentTestSetFolderPath);

		return createSpiraTestSet(
			spiraProject, testSetName, parentSpiraTestSetFolder.getID());
	}

	@Override
	public int getID() {
		return jsonObject.getInt("TestSetId");
	}

	protected static List<SpiraTestSet> getSpiraTestSets(
			SpiraProject spiraProject, SearchParameter... searchParameters)
		throws IOException {

		List<SpiraTestSet> spiraTestSets = new ArrayList<>();

		for (SpiraTestSet spiraTestSet : _spiraTestSets.values()) {
			if (spiraTestSet.matches(searchParameters)) {
				spiraTestSets.add(spiraTestSet);
			}
		}

		if (!spiraTestSets.isEmpty()) {
			return spiraTestSets;
		}

		Map<String, String> urlPathReplacements = new HashMap<>();

		urlPathReplacements.put(
			"project_id", String.valueOf(spiraProject.getID()));

		Map<String, String> urlParameters = new HashMap<>();

		urlParameters.put("number_of_rows", String.valueOf(15000));
		urlParameters.put("release_id", null);
		urlParameters.put("sort_direction", "ASC");
		urlParameters.put("sort_field", "TestSetId");
		urlParameters.put("starting_row", String.valueOf(1));

		JSONArray requestJSONArray = new JSONArray();

		for (SearchParameter searchParameter : searchParameters) {
			requestJSONArray.put(searchParameter.toFilterJSONObject());
		}

		JSONArray responseJSONArray = SpiraRestAPIUtil.requestJSONArray(
			"projects/{project_id}/test-sets/search", urlParameters,
			urlPathReplacements, HttpRequestMethod.POST,
			requestJSONArray.toString());

		for (int i = 0; i < responseJSONArray.length(); i++) {
			SpiraTestSet spiraTestSet = new SpiraTestSet(
				responseJSONArray.getJSONObject(i));

			_spiraTestSets.put(
				_createSpiraTestSetKey(
					spiraProject.getID(), spiraTestSet.getID()),
				spiraTestSet);

			if (spiraTestSet.matches(searchParameters)) {
				spiraTestSets.add(spiraTestSet);
			}
		}

		return spiraTestSets;
	}

	@Override
	protected PathSpiraArtifact getParentSpiraArtifact() {
		if (_parentSpiraArtifact != null) {
			return _parentSpiraArtifact;
		}

		Object testSetFolderID = jsonObject.get("TestSetFolderId");

		if (testSetFolderID == JSONObject.NULL) {
			return null;
		}

		if (!(testSetFolderID instanceof Integer)) {
			return null;
		}

		SpiraProject spiraProject = getSpiraProject();

		try {
			_parentSpiraArtifact = spiraProject.getSpiraTestSetFolderByID(
				(Integer)testSetFolderID);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}

		return _parentSpiraArtifact;
	}

	protected static final int STATUS_BLOCKED = 4;

	protected static final int STATUS_COMPLETED = 3;

	protected static final int STATUS_DEFERRED = 5;

	protected static final int STATUS_IN_PROGRESS = 2;

	protected static final int STATUS_NOT_STARTED = 1;

	protected static final int TEST_RUN_TYPE_AUTOMATED = 2;

	protected static final int TEST_RUN_TYPE_MANUAL = 1;

	private static String _createSpiraTestSetKey(
		Integer projectID, Integer testSetID) {

		return projectID + "-" + testSetID;
	}

	private SpiraTestSet(JSONObject jsonObject) {
		super(jsonObject);
	}

	private static final Map<String, SpiraTestSet> _spiraTestSets =
		new HashMap<>();

	private PathSpiraArtifact _parentSpiraArtifact;

}