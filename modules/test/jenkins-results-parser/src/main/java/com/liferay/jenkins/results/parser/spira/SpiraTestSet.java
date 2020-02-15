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

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class SpiraTestSet extends PathSpiraArtifact {

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