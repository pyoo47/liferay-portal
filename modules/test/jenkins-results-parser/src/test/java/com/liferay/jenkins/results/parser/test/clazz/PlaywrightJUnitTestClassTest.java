/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.test.clazz;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;
import com.liferay.jenkins.results.parser.PortalGitWorkingDirectory;
import com.liferay.jenkins.results.parser.RandomTestUtil;
import com.liferay.jenkins.results.parser.test.clazz.group.BatchTestClassGroup;

import java.io.File;

import org.json.JSONObject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mockito.Mockito;

/**
 * @author Brittney Nguyen
 */
public class PlaywrightJUnitTestClassTest
	extends com.liferay.jenkins.results.parser.Test {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		PortalGitWorkingDirectory portalGitWorkingDirectory = Mockito.mock(
			PortalGitWorkingDirectory.class);

		Mockito.doReturn(
			temporaryFolder.getRoot()
		).when(
			portalGitWorkingDirectory
		).getWorkingDirectory();

		Mockito.doReturn(
			portalGitWorkingDirectory
		).when(
			_batchTestClassGroup
		).getPortalGitWorkingDirectory();
	}

	@Test
	public void testGetJSONObject() throws Exception {
		String workspaceBundleName = RandomTestUtil.randomString();

		_testGetJSONObject(
			workspaceBundleName,
			"workspace.bundle.name=" + workspaceBundleName);

		_testGetJSONObject(null, RandomTestUtil.randomString() + "=");
	}

	@Test
	public void testGetWorkspaceBundleName() throws Exception {
		String workspaceBundleName = RandomTestUtil.randomString();

		_testGetWorkspaceBundleName(
			workspaceBundleName, null,
			"workspace.bundle.name=" + workspaceBundleName);
		_testGetWorkspaceBundleName(
			null, RandomTestUtil.randomString() + "=",
			"workspace.bundle.name=" + workspaceBundleName);

		_testGetWorkspaceBundleName(
			null, null, RandomTestUtil.randomString() + "=");
		_testGetWorkspaceBundleName(null, null, null);
	}

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private PlaywrightJUnitTestClass _newPlaywrightJUnitTestClass(
			String nestedTestPropertiesContent,
			String projectTestPropertiesContent)
		throws Exception {

		File projectDir = new File(
			temporaryFolder.getRoot(),
			"modules/test/playwright/tests/" + RandomTestUtil.randomString() +
				"/main");

		File specDir = new File(projectDir, RandomTestUtil.randomString());

		specDir.mkdirs();

		if (nestedTestPropertiesContent != null) {
			JenkinsResultsParserUtil.write(
				new File(specDir, "test.properties"),
				nestedTestPropertiesContent);
		}

		if (projectTestPropertiesContent != null) {
			JenkinsResultsParserUtil.write(
				new File(projectDir, "test.properties"),
				projectTestPropertiesContent);
		}

		File specFile = new File(
			specDir, RandomTestUtil.randomString() + ".spec.ts");

		return new PlaywrightJUnitTestClass(_batchTestClassGroup, specFile);
	}

	private void _testGetJSONObject(
			String expectedWorkspaceBundleName,
			String projectTestPropertiesContent)
		throws Exception {

		PlaywrightJUnitTestClass playwrightJUnitTestClass =
			_newPlaywrightJUnitTestClass(null, projectTestPropertiesContent);

		JSONObject jsonObject = playwrightJUnitTestClass.getJSONObject();

		PlaywrightJUnitTestClass rebuiltPlaywrightJUnitTestClass =
			new PlaywrightJUnitTestClass(
				_batchTestClassGroup, new JSONObject(jsonObject.toString()));

		testEquals(
			expectedWorkspaceBundleName,
			rebuiltPlaywrightJUnitTestClass.getWorkspaceBundleName());
	}

	private void _testGetWorkspaceBundleName(
			String expectedWorkspaceBundleName,
			String nestedTestPropertiesContent,
			String projectTestPropertiesContent)
		throws Exception {

		PlaywrightJUnitTestClass playwrightJUnitTestClass =
			_newPlaywrightJUnitTestClass(
				nestedTestPropertiesContent, projectTestPropertiesContent);

		testEquals(
			expectedWorkspaceBundleName,
			playwrightJUnitTestClass.getWorkspaceBundleName());
	}

	private final BatchTestClassGroup _batchTestClassGroup = Mockito.mock(
		BatchTestClassGroup.class);

}