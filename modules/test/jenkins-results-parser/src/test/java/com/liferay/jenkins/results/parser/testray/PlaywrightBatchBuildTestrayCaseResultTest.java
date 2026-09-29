/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import com.liferay.jenkins.results.parser.DownstreamBuildReport;
import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;
import com.liferay.jenkins.results.parser.RandomTestUtil;
import com.liferay.jenkins.results.parser.TestClassReport;
import com.liferay.jenkins.results.parser.TestReport;
import com.liferay.jenkins.results.parser.test.clazz.PlaywrightJUnitTestClass;
import com.liferay.jenkins.results.parser.test.clazz.PlaywrightTestClassMethod;
import com.liferay.jenkins.results.parser.test.clazz.group.AxisTestClassGroup;
import com.liferay.jenkins.results.parser.test.clazz.group.PlaywrightSegmentTestClassGroup;
import com.liferay.jenkins.results.parser.test.clazz.group.SegmentTestClassGroup;

import java.io.File;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Calum Ragan
 */
public class PlaywrightBatchBuildTestrayCaseResultTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testFindTestReport() {
		String projectName = RandomTestUtil.randomString();
		String specFilePath =
			RandomTestUtil.randomString() + "/" + RandomTestUtil.randomString();
		String testName = RandomTestUtil.randomString();

		PlaywrightBatchBuildTestrayCaseResult
			playwrightBatchBuildTestrayCaseResult =
				_mockPlaywrightBatchBuildTestrayCaseResult(
					new TreeSet<>(
						Arrays.asList(
							projectName, RandomTestUtil.randomString())),
					_mockPlaywrightSegmentTestClassGroup(projectName),
					specFilePath, testName);

		Mockito.doCallRealMethod(
		).when(
			playwrightBatchBuildTestrayCaseResult
		).findTestReport();

		TestReport testReport = _mockTestReport(specFilePath, testName);

		TestClassReport testClassReport = Mockito.mock(TestClassReport.class);

		Mockito.doReturn(
			specFilePath
		).when(
			testClassReport
		).getTestClassName();

		Mockito.doReturn(
			Arrays.asList(
				_mockTestReport(specFilePath, RandomTestUtil.randomString()),
				testReport)
		).when(
			testClassReport
		).getTestReports();

		DownstreamBuildReport downstreamBuildReport = Mockito.mock(
			DownstreamBuildReport.class);

		Mockito.doReturn(
			Collections.singletonList(testClassReport)
		).when(
			downstreamBuildReport
		).getTestClassReports();

		Mockito.doReturn(
			downstreamBuildReport
		).when(
			playwrightBatchBuildTestrayCaseResult
		).getDownstreamBuildReport();

		Assert.assertSame(
			testReport, playwrightBatchBuildTestrayCaseResult.findTestReport());
	}

	@Test
	public void testGetName() {
		String projectDirName = RandomTestUtil.randomString();
		String projectSubdirName = RandomTestUtil.randomString();
		String projectSuffix1 = RandomTestUtil.randomString();
		String projectSuffix2 = RandomTestUtil.randomString();
		String specFileName = RandomTestUtil.randomString();
		String testName = RandomTestUtil.randomString();

		String projectName1 = projectDirName + "." + projectSuffix1;
		String projectName2 = JenkinsResultsParserUtil.combine(
			projectDirName, ".", projectSubdirName, ".", projectSuffix2);
		String specFilePath =
			RandomTestUtil.randomString() + "/" + specFileName;

		Set<String> projectNames = new TreeSet<>(
			Arrays.asList(projectName1, projectName2));

		_testGetName(
			JenkinsResultsParserUtil.combine(
				projectDirName, "/", projectSubdirName, "/", projectSuffix2,
				"/", specFileName, " > ", testName),
			projectNames, _mockPlaywrightSegmentTestClassGroup(projectName2),
			specFilePath, testName);
		_testGetName(
			JenkinsResultsParserUtil.combine(
				projectDirName, "/", projectSuffix1, "/", specFileName, " > ",
				testName),
			projectNames, _mockPlaywrightSegmentTestClassGroup(projectName1),
			JenkinsResultsParserUtil.combine(
				RandomTestUtil.randomString(), "/",
				RandomTestUtil.randomString(), "/", specFileName),
			testName);
		_testGetName(
			JenkinsResultsParserUtil.combine(
				projectDirName, "/", projectSuffix1, "/", specFileName, " > ",
				testName),
			projectNames, _mockPlaywrightSegmentTestClassGroup(projectName1),
			specFilePath, testName);

		_testGetName(
			specFilePath + " > " + testName, Collections.emptySet(),
			_mockPlaywrightSegmentTestClassGroup(projectName1), specFilePath,
			testName);
		_testGetName(
			specFilePath + " > " + testName,
			Collections.singleton(projectName1),
			_mockPlaywrightSegmentTestClassGroup(projectName1), specFilePath,
			testName);
		_testGetName(
			specFilePath + " > " + testName, projectNames,
			Mockito.mock(SegmentTestClassGroup.class), specFilePath, testName);
		_testGetName(
			specFilePath + " > " + testName, projectNames,
			_mockPlaywrightSegmentTestClassGroup(null), specFilePath, testName);
	}

	private PlaywrightBatchBuildTestrayCaseResult
		_mockPlaywrightBatchBuildTestrayCaseResult(
			Set<String> projectNames,
			SegmentTestClassGroup segmentTestClassGroup, String specFilePath,
			String testName) {

		AxisTestClassGroup axisTestClassGroup = Mockito.mock(
			AxisTestClassGroup.class);

		Mockito.doReturn(
			segmentTestClassGroup
		).when(
			axisTestClassGroup
		).getSegmentTestClassGroup();

		PlaywrightJUnitTestClass playwrightJUnitTestClass = Mockito.mock(
			PlaywrightJUnitTestClass.class);

		Mockito.doReturn(
			projectNames
		).when(
			playwrightJUnitTestClass
		).getProjectNames();

		Mockito.doReturn(
			specFilePath
		).when(
			playwrightJUnitTestClass
		).getSpecFilePath();

		Mockito.doReturn(
			new File(specFilePath)
		).when(
			playwrightJUnitTestClass
		).getTestClassFile();

		PlaywrightTestClassMethod playwrightTestClassMethod = Mockito.mock(
			PlaywrightTestClassMethod.class);

		Mockito.doReturn(
			specFilePath + " > " + testName
		).when(
			playwrightTestClassMethod
		).getName();

		Mockito.doReturn(
			testName
		).when(
			playwrightTestClassMethod
		).getTestName();

		PlaywrightBatchBuildTestrayCaseResult
			playwrightBatchBuildTestrayCaseResult = Mockito.mock(
				PlaywrightBatchBuildTestrayCaseResult.class);

		Mockito.doReturn(
			axisTestClassGroup
		).when(
			playwrightBatchBuildTestrayCaseResult
		).getAxisTestClassGroup();

		Mockito.doCallRealMethod(
		).when(
			playwrightBatchBuildTestrayCaseResult
		).getName();

		Mockito.doReturn(
			playwrightJUnitTestClass
		).when(
			playwrightBatchBuildTestrayCaseResult
		).getTestClass();

		Mockito.doReturn(
			playwrightTestClassMethod
		).when(
			playwrightBatchBuildTestrayCaseResult
		).getTestClassMethod();

		return playwrightBatchBuildTestrayCaseResult;
	}

	private PlaywrightSegmentTestClassGroup
		_mockPlaywrightSegmentTestClassGroup(String projectName) {

		PlaywrightSegmentTestClassGroup playwrightSegmentTestClassGroup =
			Mockito.mock(PlaywrightSegmentTestClassGroup.class);

		Mockito.doReturn(
			projectName
		).when(
			playwrightSegmentTestClassGroup
		).getProjectName();

		return playwrightSegmentTestClassGroup;
	}

	private TestReport _mockTestReport(String testClassName, String testName) {
		TestReport testReport = Mockito.mock(TestReport.class);

		Mockito.doReturn(
			testClassName
		).when(
			testReport
		).getTestClassName();

		Mockito.doReturn(
			testName
		).when(
			testReport
		).getTestName();

		return testReport;
	}

	private void _testGetName(
		String expectedName, Set<String> projectNames,
		SegmentTestClassGroup segmentTestClassGroup, String specFilePath,
		String testName) {

		PlaywrightBatchBuildTestrayCaseResult
			playwrightBatchBuildTestrayCaseResult =
				_mockPlaywrightBatchBuildTestrayCaseResult(
					projectNames, segmentTestClassGroup, specFilePath,
					testName);

		Assert.assertEquals(
			expectedName, playwrightBatchBuildTestrayCaseResult.getName());
	}

}