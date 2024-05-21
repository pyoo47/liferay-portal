/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import java.net.URL;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class SaaSTestrayProject extends BaseTestrayProject {

	public SaaSTestrayProject(
		TestrayServer testrayServer, JSONObject jsonObject) {

		super(testrayServer);

		_id = jsonObject.getLong("id");
	}

	@Override
	public TestrayProductVersion createTestrayProductVersion(
		String testrayProductVersionName) {

		return null;
	}

	@Override
	public TestrayRoutine createTestrayRoutine(String testrayRoutineName) {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public long getID() {
		return _id;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public TestrayProductVersion getTestrayProductVersionByID(
		long productVersionID) {

		return null;
	}

	@Override
	public TestrayProductVersion getTestrayProductVersionByName(
		String productVersionName) {

		return null;
	}

	@Override
	public TestrayRoutine getTestrayRoutineByID(long routineID) {
		return null;
	}

	@Override
	public TestrayRoutine getTestrayRoutineByName(String routineName) {
		return null;
	}

	@Override
	public URL getURL() {
		return null;
	}

	private final long _id;

}