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

package com.liferay.jenkins.results.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;

import java.net.URL;

import java.util.Hashtable;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Peter Yoo
 */
public class BuildTest extends BaseJenkinsResultsParserTestCase {

	@Before
	public void setUp() throws Exception {
		JenkinsResultsParserUtil.setBuildProperties(
			JenkinsResultsParserUtil.getBuildProperties());

		downloadSample(
			"test-jenkins-acceptance-pullrequest_passed", "117",
			"test-jenkins-acceptance-pullrequest", "test-1-17");
		downloadSample(
			"test-plugins-acceptance-pullrequest(ee-6.2.x)_passed", "66",
			"test-plugins-acceptance-pullrequest(ee-6.2.x)", "test-1-8");
		downloadSample(
			"test-portal-acceptance-pullrequest(7.0.x)_unresolved-req-failure",
			"103", "test-portal-acceptance-pullrequest(7.0.x)", "test-1-14");
		downloadSample(
			"test-portal-acceptance-pullrequest(7.0.x-private)" +
				"_validation-compile-failure",
			"94", "test-portal-acceptance-pullrequest(7.0.x-private)",
			"test-1-5");
		downloadSample(
			"test-portal-acceptance-pullrequest(7.0.x-private)" +
				"_validation-no-unit",
			"70", "test-portal-acceptance-pullrequest(7.0.x-private)",
			"test-1-13");
		downloadSample(
			"test-portal-acceptance-pullrequest(7.0.x-private)" +
				"_validation-passed",
			"77", "test-portal-acceptance-pullrequest(7.0.x-private)",
			"test-1-10");
		downloadSample(
			"test-portal-acceptance-pullrequest(7.0.x-private)" +
				"_validation-unit-failure",
			"78", "test-portal-acceptance-pullrequest(7.0.x-private)",
			"test-1-10");
		downloadSample(
			"test-portal-acceptance-pullrequest(ee-6.2.x)_passed", "337",
			"test-portal-acceptance-pullrequest(ee-6.2.x)", "test-1-17");
		downloadSample(
			"test-portal-acceptance-pullrequest(master)_generic-failure",
			"1375", "test-portal-acceptance-pullrequest(master)", "test-1-1");
		downloadSample(
			"test-portal-acceptance-pullrequest(master)" +
				"_modules-compile-failure",
			"999", "test-portal-acceptance-pullrequest(master)", "test-1-21");
		downloadSample(
			"test-portal-acceptance-pullrequest(master)_passed", "446",
			"test-portal-acceptance-pullrequest(master)", "test-1-8");
		downloadSample(
			"test-portal-acceptance-pullrequest(master)_poshi-test-failure",
			"1268", "test-portal-acceptance-pullrequest(master)", "test-1-9");
		downloadSample(
			"test-portal-acceptance-pullrequest(master)" +
				"_semantic_versioning_failure",
			"2003", "test-portal-acceptance-pullrequest(master)", "test-1-3");
		downloadSample(
			"test-portal-acceptance-pullrequest(master)_source-format-failure",
			"2209", "test-portal-acceptance-pullrequest(master)", "test-1-2");
	}

	@After
	public void tearDown() throws Exception {
		JenkinsResultsParserUtil.setBuildProperties((Hashtable<?, ?>)null);
	}

	@Test
	public void testGetGitHubMessage() throws Exception {
		setJenkinsResultsParserExpectedMessageGenerator(
			new JenkinsResultsParserExpectedMessageGenerator() {

				@Override
				public String getMessage(String sampleKey) throws Exception {
					Build build = BuildFactory.newBuildFromArchive(
						"BuildTest/" + sampleKey);

					build.setCompareToUpstream(false);

					return Dom4JUtil.format(
						build.getGitHubMessageElement(), true);
				}

			});

		assertSample("test-jenkins-acceptance-pullrequest_passed");
		assertSample("test-plugins-acceptance-pullrequest(ee-6.2.x)_passed");
		assertSample(
			"test-portal-acceptance-pullrequest(7.0.x)_unresolved-req-failure");
		assertSample("test-portal-acceptance-pullrequest(ee-6.2.x)_passed");
		assertSample(
			"test-portal-acceptance-pullrequest(master)_generic-failure");
		assertSample(
			"test-portal-acceptance-pullrequest(master)" +
				"_modules-compile-failure");
		assertSample("test-portal-acceptance-pullrequest(master)_passed");
		assertSample(
			"test-portal-acceptance-pullrequest(master)_poshi-test-failure");
		assertSample(
			"test-portal-acceptance-pullrequest(master)" +
				"_semantic_versioning_failure");
		assertSample(
			"test-portal-acceptance-pullrequest(master)_source-format-failure");
	}

	@Test
	public void testGetValidationGitHubMessage() throws Exception {
		setExpectedMessageFileName("expected_validation_message.html");

		setJenkinsResultsParserExpectedMessageGenerator(
			new JenkinsResultsParserExpectedMessageGenerator() {

				@Override
				public String getMessage(String sampleKey) throws Exception {
					TopLevelBuild topLevelBuild =
						(TopLevelBuild)BuildFactory.newBuildFromArchive(
							"BuildTest/" + sampleKey);

					topLevelBuild.setCompareToUpstream(false);

					return Dom4JUtil.format(
						topLevelBuild.getValidationGitHubMessageElement(),
						true);
				}

			});

		assertSample(
			"test-portal-acceptance-pullrequest(7.0.x-private)" +
				"_validation-compile-failure");
		assertSample(
			"test-portal-acceptance-pullrequest(7.0.x-private)" +
				"_validation-no-unit");
		assertSample(
			"test-portal-acceptance-pullrequest(7.0.x-private)" +
				"_validation-passed");
		assertSample(
			"test-portal-acceptance-pullrequest(7.0.x-private)" +
				"_validation-unit-failure");
	}

	@Override
	protected void downloadSample(File sampleDir, URL url) throws Exception {
		Build build = BuildFactory.newBuild(
			JenkinsResultsParserUtil.getLocalURL(url.toExternalForm()), null);

		build.archive(getSimpleClassName() + "/" + sampleDir.getName());
	}

	protected Properties loadProperties(String sampleName) throws Exception {
		Class<?> clazz = getClass();

		Properties properties = new Properties();

		String content = JenkinsResultsParserUtil.toString(
			JenkinsResultsParserUtil.getLocalURL(
				JenkinsResultsParserUtil.combine(
					"${dependencies.url}", clazz.getSimpleName(), "/",
					sampleName, "/sample.properties")));

		properties.load(new StringReader(content));

		return properties;
	}

	protected void saveProperties(File file, Properties properties)
		throws Exception {

		try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
			properties.store(fileOutputStream, null);
		}
	}

}