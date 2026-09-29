/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.test.clazz.group;

import com.liferay.jenkins.results.parser.Job;
import com.liferay.jenkins.results.parser.RandomTestUtil;
import com.liferay.jenkins.results.parser.job.property.JobProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Brittney Nguyen
 */
public class PlaywrightSegmentTestClassGroupTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testGetTestCasePropertiesContent() {
		String projectName = RandomTestUtil.randomString();

		String testCasePropertiesContent = _getTestCasePropertiesContent(
			projectName, null);

		Assert.assertFalse(
			testCasePropertiesContent.contains(
				"PLAYWRIGHT_WORKSPACE_BUNDLE_NAME"));
		Assert.assertTrue(
			testCasePropertiesContent.endsWith(
				"PLAYWRIGHT_PROJECT_NAME=" + projectName));

		String workspaceBundleName = RandomTestUtil.randomString();

		testEquals(
			testCasePropertiesContent + "\nPLAYWRIGHT_WORKSPACE_BUNDLE_NAME=" +
				workspaceBundleName,
			_getTestCasePropertiesContent(projectName, workspaceBundleName));

		testEquals(
			testCasePropertiesContent,
			_getTestCasePropertiesContent(projectName, ""));
	}

	@Test
	public void testGetWorkspaceBundleName() {
		String workspaceBundleName1 = RandomTestUtil.randomString();

		_testGetWorkspaceBundleName(
			workspaceBundleName1,
			Arrays.asList(null, "", workspaceBundleName1));

		String workspaceBundleName2 = RandomTestUtil.randomString();

		_testGetWorkspaceBundleName(
			workspaceBundleName1,
			Arrays.asList(workspaceBundleName1, workspaceBundleName2));

		_testGetWorkspaceBundleName(null, Arrays.asList("", null));
		_testGetWorkspaceBundleName(null, Collections.emptyList());
	}

	private String _getTestCasePropertiesContent(
		String projectName, String workspaceBundleName) {

		PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
			Mockito.mock(PlaywrightBatchTestClassGroup.class);

		Mockito.doReturn(
			Mockito.mock(JobProperty.class)
		).when(
			playwrightBatchTestClassGroup
		).getJobProperty(
			Mockito.anyString(), Mockito.nullable(String.class),
			Mockito.nullable(String.class)
		);

		PlaywrightSegmentTestClassGroup playwrightSegmentTestClassGroup =
			Mockito.mock(PlaywrightSegmentTestClassGroup.class);

		Mockito.doReturn(
			playwrightBatchTestClassGroup
		).when(
			playwrightSegmentTestClassGroup
		).getBatchTestClassGroup();

		Mockito.doReturn(
			Mockito.mock(Job.class)
		).when(
			playwrightSegmentTestClassGroup
		).getJob();

		Mockito.doReturn(
			projectName
		).when(
			playwrightSegmentTestClassGroup
		).getProjectName();

		if (workspaceBundleName != null) {
			Mockito.doReturn(
				workspaceBundleName
			).when(
				playwrightSegmentTestClassGroup
			).getWorkspaceBundleName();
		}

		Mockito.doCallRealMethod(
		).when(
			playwrightSegmentTestClassGroup
		).getTestCasePropertiesContent();

		return playwrightSegmentTestClassGroup.getTestCasePropertiesContent();
	}

	private void _testGetWorkspaceBundleName(
		String expectedWorkspaceBundleName, List<String> workspaceBundleNames) {

		List<AxisTestClassGroup> axisTestClassGroups = new ArrayList<>();

		for (String workspaceBundleName : workspaceBundleNames) {
			PlaywrightAxisTestClassGroup playwrightAxisTestClassGroup =
				Mockito.mock(PlaywrightAxisTestClassGroup.class);

			if (workspaceBundleName != null) {
				Mockito.doReturn(
					workspaceBundleName
				).when(
					playwrightAxisTestClassGroup
				).getWorkspaceBundleName();
			}

			axisTestClassGroups.add(playwrightAxisTestClassGroup);
		}

		PlaywrightSegmentTestClassGroup playwrightSegmentTestClassGroup =
			Mockito.mock(PlaywrightSegmentTestClassGroup.class);

		Mockito.doReturn(
			axisTestClassGroups
		).when(
			playwrightSegmentTestClassGroup
		).getAxisTestClassGroups();

		Mockito.doCallRealMethod(
		).when(
			playwrightSegmentTestClassGroup
		).getWorkspaceBundleName();

		testEquals(
			expectedWorkspaceBundleName,
			playwrightSegmentTestClassGroup.getWorkspaceBundleName());
	}

}