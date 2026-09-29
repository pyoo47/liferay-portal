/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.test.clazz;

import com.liferay.jenkins.results.parser.PortalGitWorkingDirectory;
import com.liferay.jenkins.results.parser.RandomTestUtil;
import com.liferay.jenkins.results.parser.test.clazz.group.PlaywrightBatchTestClassGroup;

import java.io.File;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONObject;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Calum Ragan
 */
public class PlaywrightJUnitTestClassTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testGetProjectNamesJSONObject() {
		PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
			_mockPlaywrightBatchTestClassGroup();

		PlaywrightJUnitTestClass playwrightJUnitTestClass =
			new PlaywrightJUnitTestClass(
				playwrightBatchTestClassGroup,
				new File(RandomTestUtil.randomString()));

		Set<String> projectNames = new TreeSet<>(
			Arrays.asList(
				RandomTestUtil.randomString(), RandomTestUtil.randomString()));

		for (String projectName : projectNames) {
			playwrightJUnitTestClass.addProjectName(projectName);
		}

		PlaywrightJUnitTestClass jsonPlaywrightJUnitTestClass =
			new PlaywrightJUnitTestClass(
				playwrightBatchTestClassGroup,
				new JSONObject(
					String.valueOf(playwrightJUnitTestClass.getJSONObject())));

		Assert.assertEquals(
			projectNames, jsonPlaywrightJUnitTestClass.getProjectNames());
	}

	@Test
	public void testGetProjectNamesJSONObjectDefault() {
		PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
			_mockPlaywrightBatchTestClassGroup();

		PlaywrightJUnitTestClass playwrightJUnitTestClass =
			new PlaywrightJUnitTestClass(
				playwrightBatchTestClassGroup,
				new File(RandomTestUtil.randomString()));

		JSONObject jsonObject = playwrightJUnitTestClass.getJSONObject();

		jsonObject.remove("project_names");

		PlaywrightJUnitTestClass jsonPlaywrightJUnitTestClass =
			new PlaywrightJUnitTestClass(
				playwrightBatchTestClassGroup,
				new JSONObject(String.valueOf(jsonObject)));

		Set<String> projectNames =
			jsonPlaywrightJUnitTestClass.getProjectNames();

		Assert.assertTrue(projectNames.isEmpty());
	}

	private PlaywrightBatchTestClassGroup _mockPlaywrightBatchTestClassGroup() {
		PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
			Mockito.mock(PlaywrightBatchTestClassGroup.class);

		Mockito.doReturn(
			Mockito.mock(PortalGitWorkingDirectory.class)
		).when(
			playwrightBatchTestClassGroup
		).getPortalGitWorkingDirectory();

		return playwrightBatchTestClassGroup;
	}

}