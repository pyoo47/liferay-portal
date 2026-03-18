/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.test.batch;

import com.liferay.jenkins.results.parser.job.property.JobProperty;
import com.liferay.jenkins.results.parser.test.suite.RelevantRuleConfigurationException;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Kenji Heigel
 */
public class JUnitTestSelector extends BaseTestSelector {

	public JUnitTestSelector(
			File propertiesFile, Properties properties, String batchName,
			String relevantRuleName, String testSuiteName)
		throws RelevantRuleConfigurationException {

		super(
			propertiesFile, properties, batchName, relevantRuleName,
			testSuiteName);

		validate();

		_addJobProperties();
	}

	public List<JobProperty> getExcludesJobProperties() {
		JobProperty jobProperty = getGlobalJobProperty(
			"test.batch.class.names.excludes", JobProperty.Type.EXCLUDE_GLOB);

		if (!_excludesJobProperties.contains(jobProperty)) {
			_excludesJobProperties.add(jobProperty);
		}

		return _excludesJobProperties;
	}

	public JobProperty getExcludesJobProperty() {
		return getJobProperty(
			_MODULES_INCLUDES_REQUIRED_TEST_BATCH_CLASS_NAMES_EXCLUDES,
			JobProperty.Type.MODULE_EXCLUDE_GLOB);
	}

	public List<JobProperty> getIncludesJobProperties() {
		return _includesJobProperties;
	}

	public JobProperty getIncludesJobProperty() {
		return getJobProperty(
			_MODULES_INCLUDES_REQUIRED_TEST_BATCH_CLASS_NAMES_INCLUDES,
			JobProperty.Type.MODULE_INCLUDE_GLOB);
	}

	@Override
	public void merge(TestSelector testSelector) {
		if (!(testSelector instanceof JUnitTestSelector)) {
			throw new RuntimeException("Unable to merge test selectors");
		}

		JUnitTestSelector jUnitTestSelector = (JUnitTestSelector)testSelector;

		if (!_includesJobProperties.contains(
				jUnitTestSelector.getIncludesJobProperty())) {

			_includesJobProperties.add(
				jUnitTestSelector.getIncludesJobProperty());
		}

		if (!_excludesJobProperties.contains(
				jUnitTestSelector.getExcludesJobProperty())) {

			_excludesJobProperties.add(
				jUnitTestSelector.getExcludesJobProperty());
		}
	}

	@Override
	public void validate() throws RelevantRuleConfigurationException {
		String modulesIncludesRequiredTestBatchClassNamesIncludes = getProperty(
			_MODULES_INCLUDES_REQUIRED_TEST_BATCH_CLASS_NAMES_INCLUDES);
		String testBatchClassNamesIncludesRequired = getProperty(
			_TEST_BATCH_CLASS_NAMES_INCLUDES_REQUIRED);

		if ((modulesIncludesRequiredTestBatchClassNamesIncludes == null) &&
			(testBatchClassNamesIncludesRequired == null)) {

			StringBuilder sb = new StringBuilder();

			sb.append("Unable to create batch ");
			sb.append(getBatchName());
			sb.append(" since ");
			sb.append(
				_MODULES_INCLUDES_REQUIRED_TEST_BATCH_CLASS_NAMES_INCLUDES);
			sb.append(" or ");
			sb.append(_TEST_BATCH_CLASS_NAMES_INCLUDES_REQUIRED);
			sb.append(" is not set in ");
			sb.append(getPropertiesFile());

			throw new RelevantRuleConfigurationException(sb.toString());
		}
	}

	private void _addJobProperties() {
		_excludesJobProperties.add(getExcludesJobProperty());
		_includesJobProperties.add(getIncludesJobProperty());

		_includesJobProperties.add(
			getJobProperty(
				_TEST_BATCH_CLASS_NAMES_INCLUDES_REQUIRED,
				JobProperty.Type.MODULE_INCLUDE_GLOB));
	}

	private static final String
		_MODULES_INCLUDES_REQUIRED_TEST_BATCH_CLASS_NAMES_EXCLUDES =
			"modules.includes.required.test.batch.class.names.excludes";

	private static final String
		_MODULES_INCLUDES_REQUIRED_TEST_BATCH_CLASS_NAMES_INCLUDES =
			"modules.includes.required.test.batch.class.names.includes";

	private static final String _TEST_BATCH_CLASS_NAMES_INCLUDES_REQUIRED =
		"test.batch.class.names.includes.required";

	private final List<JobProperty> _excludesJobProperties = new ArrayList<>();
	private final List<JobProperty> _includesJobProperties = new ArrayList<>();

}