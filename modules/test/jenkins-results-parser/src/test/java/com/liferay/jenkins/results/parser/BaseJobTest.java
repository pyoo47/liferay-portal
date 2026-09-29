/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import com.liferay.jenkins.results.parser.test.clazz.group.BatchTestClassGroup;
import com.liferay.jenkins.results.parser.test.clazz.group.PlaywrightSegmentTestClassGroup;
import com.liferay.jenkins.results.parser.test.clazz.group.SegmentTestClassGroup;

import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Brittney Nguyen
 */
public class BaseJobTest extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testGetWorkspaceBundleNames() {
		String workspaceBundleName1 = RandomTestUtil.randomString();
		String workspaceBundleName2 = RandomTestUtil.randomString();

		_testGetWorkspaceBundleNames(
			new TreeSet<>(
				Arrays.asList(workspaceBundleName1, workspaceBundleName2)),
			Arrays.asList(
				Arrays.asList(workspaceBundleName1, null, workspaceBundleName2),
				Arrays.asList(workspaceBundleName1)));

		_testGetWorkspaceBundleNames(
			Collections.emptySet(), Arrays.asList(Arrays.asList("", null)));
		_testGetWorkspaceBundleNames(
			Collections.emptySet(), Collections.emptyList());
	}

	@Test
	public void testGetWorkspaceBundleNamesSegmentTypes() throws Exception {
		Set<String> expectedWorkspaceBundleNames = new TreeSet<>();
		List<SegmentTestClassGroup> segmentTestClassGroups = new ArrayList<>();

		for (Class<?> clazz : _getSegmentTestClassGroupClasses()) {
			SegmentTestClassGroup segmentTestClassGroup =
				(SegmentTestClassGroup)Mockito.mock(clazz);

			if (segmentTestClassGroup instanceof
					PlaywrightSegmentTestClassGroup) {

				String workspaceBundleName = RandomTestUtil.randomString();

				Mockito.doReturn(
					workspaceBundleName
				).when(
					(PlaywrightSegmentTestClassGroup)segmentTestClassGroup
				).getWorkspaceBundleName();

				expectedWorkspaceBundleNames.add(workspaceBundleName);
			}

			segmentTestClassGroups.add(segmentTestClassGroup);
		}

		Assert.assertTrue(expectedWorkspaceBundleNames.size() > 1);
		Assert.assertTrue(
			segmentTestClassGroups.size() >
				expectedWorkspaceBundleNames.size());

		testEquals(
			expectedWorkspaceBundleNames,
			_getWorkspaceBundleNames(
				Collections.singletonList(segmentTestClassGroups)));
	}

	private List<Class<?>> _getSegmentTestClassGroupClasses() throws Exception {
		List<Class<?>> segmentTestClassGroupClasses = new ArrayList<>();

		URL url = SegmentTestClassGroup.class.getResource(
			"SegmentTestClassGroup.class");

		File file = new File(url.toURI());

		File parentFile = file.getParentFile();

		Package pkg = SegmentTestClassGroup.class.getPackage();

		for (String fileName :
				new TreeSet<>(Arrays.asList(parentFile.list()))) {

			if (!fileName.endsWith(".class") || fileName.contains("$")) {
				continue;
			}

			Class<?> clazz = Class.forName(
				JenkinsResultsParserUtil.combine(
					pkg.getName(), ".",
					fileName.substring(0, fileName.length() - 6)));

			if (SegmentTestClassGroup.class.isAssignableFrom(clazz)) {
				segmentTestClassGroupClasses.add(clazz);
			}
		}

		return segmentTestClassGroupClasses;
	}

	private Set<String> _getWorkspaceBundleNames(
		List<List<SegmentTestClassGroup>> segmentTestClassGroupsList) {

		List<BatchTestClassGroup> batchTestClassGroups = new ArrayList<>();

		for (List<SegmentTestClassGroup> segmentTestClassGroups :
				segmentTestClassGroupsList) {

			BatchTestClassGroup batchTestClassGroup = Mockito.mock(
				BatchTestClassGroup.class);

			Mockito.doReturn(
				segmentTestClassGroups
			).when(
				batchTestClassGroup
			).getSegmentTestClassGroups();

			batchTestClassGroups.add(batchTestClassGroup);
		}

		BaseJob baseJob = Mockito.mock(BaseJob.class);

		Mockito.doReturn(
			batchTestClassGroups
		).when(
			baseJob
		).getBatchTestClassGroups();

		Mockito.doCallRealMethod(
		).when(
			baseJob
		).getSegmentTestClassGroups();

		Mockito.doCallRealMethod(
		).when(
			baseJob
		).getWorkspaceBundleNames();

		return baseJob.getWorkspaceBundleNames();
	}

	private void _testGetWorkspaceBundleNames(
		Set<String> expectedWorkspaceBundleNames,
		List<List<String>> workspaceBundleNamesList) {

		List<List<SegmentTestClassGroup>> segmentTestClassGroupsList =
			new ArrayList<>();

		for (List<String> workspaceBundleNames : workspaceBundleNamesList) {
			List<SegmentTestClassGroup> segmentTestClassGroups =
				new ArrayList<>();

			for (String workspaceBundleName : workspaceBundleNames) {
				PlaywrightSegmentTestClassGroup
					playwrightSegmentTestClassGroup = Mockito.mock(
						PlaywrightSegmentTestClassGroup.class);

				if (workspaceBundleName != null) {
					Mockito.doReturn(
						workspaceBundleName
					).when(
						playwrightSegmentTestClassGroup
					).getWorkspaceBundleName();
				}

				segmentTestClassGroups.add(playwrightSegmentTestClassGroup);
			}

			segmentTestClassGroupsList.add(segmentTestClassGroups);
		}

		testEquals(
			expectedWorkspaceBundleNames,
			_getWorkspaceBundleNames(segmentTestClassGroupsList));
	}

}