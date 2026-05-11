/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.io.IOException;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class RunTestrayFactor extends BaseTestrayFactor {

	@Override
	public Category getCategory() {
		Option option = getOption();

		return option.getCategory();
	}

	@Override
	public JSONObject getJSONObject() {
		if (_jsonObject != null) {
			return _jsonObject;
		}

		JSONObject baseJSONObject = super.getJSONObject();

		if (baseJSONObject != null) {
			_jsonObject = baseJSONObject;

			return _jsonObject;
		}

		TestrayServer testrayServer = _testrayBuild.getTestrayServer();

		Category category = getCategory();

		Option option = getOption();

		TestrayRun testrayRun = getTestrayRun();

		String filter = JenkinsResultsParserUtil.combine(
			"r_factorCategoryToFactors_c_factorCategoryId eq '",
			String.valueOf(category.getID()),
			"' and r_factorOptionToFactors_c_factorOptionId eq '",
			String.valueOf(option.getID()), "' and r_runToFactors_c_runId eq '",
			String.valueOf(testrayRun.getID()), "'");

		try {
			JSONObject jsonObject = new JSONObject(
				testrayServer.requestGet(
					"/o/c/factors?filter=" +
						URLEncoder.encode(filter, "UTF-8")));

			JSONArray itemsJSONArray = jsonObject.optJSONArray("items");

			if ((itemsJSONArray != null) && !itemsJSONArray.isEmpty()) {
				_jsonObject = itemsJSONArray.getJSONObject(0);

				return _jsonObject;
			}

			JSONObject requestJSONObject = new JSONObject();

			requestJSONObject.put(
				"r_factorCategoryToFactors_c_factorCategoryId", category.getID()
			).put(
				"r_factorOptionToFactors_c_factorOptionId", option.getID()
			).put(
				"r_runToFactors_c_runId", testrayRun.getID()
			);

			_jsonObject = new JSONObject(
				testrayServer.requestPost(
					"/o/c/factors", requestJSONObject.toString()));

			return _jsonObject;
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	@Override
	public Option getOption() {
		if (_option != null) {
			return _option;
		}

		return super.getOption();
	}

	public TestrayRun getTestrayRun() {
		return _testrayRun;
	}

	protected RunTestrayFactor(JSONObject jsonObject, TestrayRun testrayRun) {
		super(testrayRun.getTestrayServer(), jsonObject);

		_jsonObject = jsonObject;
		_testrayRun = testrayRun;

		_testrayBuild = testrayRun.getTestrayBuild();

		_option = super.getOption();
	}

	protected RunTestrayFactor(
		TestrayRun testrayRun, TestrayFactor.Option option) {

		super(testrayRun.getTestrayServer());

		_testrayRun = testrayRun;
		_option = option;

		_testrayBuild = testrayRun.getTestrayBuild();
	}

	private JSONObject _jsonObject;
	private final Option _option;
	private final TestrayBuild _testrayBuild;
	private final TestrayRun _testrayRun;

}