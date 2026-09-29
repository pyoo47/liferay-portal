/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.test.clazz.group;

import com.liferay.jenkins.results.parser.RandomTestUtil;
import com.liferay.jenkins.results.parser.test.clazz.PlaywrightJUnitTestClass;
import com.liferay.jenkins.results.parser.test.clazz.TestClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Brittney Nguyen
 */
public class PlaywrightAxisTestClassGroupTest
	extends com.liferay.jenkins.results.parser.Test {

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

	private void _testGetWorkspaceBundleName(
		String expectedWorkspaceBundleName, List<String> workspaceBundleNames) {

		List<TestClass> testClasses = new ArrayList<>();

		for (String workspaceBundleName : workspaceBundleNames) {
			PlaywrightJUnitTestClass playwrightJUnitTestClass = Mockito.mock(
				PlaywrightJUnitTestClass.class);

			if (workspaceBundleName != null) {
				Mockito.doReturn(
					workspaceBundleName
				).when(
					playwrightJUnitTestClass
				).getWorkspaceBundleName();
			}

			testClasses.add(playwrightJUnitTestClass);
		}

		PlaywrightAxisTestClassGroup playwrightAxisTestClassGroup =
			Mockito.mock(PlaywrightAxisTestClassGroup.class);

		Mockito.doReturn(
			testClasses
		).when(
			playwrightAxisTestClassGroup
		).getTestClasses();

		Mockito.doCallRealMethod(
		).when(
			playwrightAxisTestClassGroup
		).getWorkspaceBundleName();

		testEquals(
			expectedWorkspaceBundleName,
			playwrightAxisTestClassGroup.getWorkspaceBundleName());
	}

}