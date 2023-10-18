/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.metrics;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/**
 * @author Kenji Heigel
 */
public class Report {

	public static Report newBuildHistoryReport(
		File outputDir, String startDateString, long durationDays) {

		Report report = new Report(outputDir);

		report.addFileFromResource("css/report.css", "dependencies/metrics/");

		report.addFileFromResource(
			"css/main.css", "dependencies/metrics/build-history-report/");
		report.addFileFromResource(
			"index.html", "dependencies/metrics/build-history-report/");

		report.addFileFromResource(
			"js/main.js", "dependencies/metrics/build-history-report/");

		report.addFileFromResource("js/utils.js", "dependencies/metrics/");

		JobHistoryDataset jobHistoryDataset =
			JobHistoryDataset.newAggregateDataset(
				startDateString, durationDays);

		report.addFile(
			"js/table-data.js", jobHistoryDataset.getTableDataJSFileContent());

		report.addFile(
			"js/timeline-data.js",
			jobHistoryDataset.getTimelineDataJSFileContent());

		return report;
	}

	public static Report newTestSuiteReport(
		File outputDir, String startDateString, long durationDays) {

		Report report = new Report(outputDir);

		report.addFileFromResource("css/report.css", "dependencies/metrics/");

		report.addFileFromResource(
			"css/main.css", "dependencies/metrics/test-suite-report/");

		report.addFileFromResource(
			"index.html", "dependencies/metrics/test-suite-report/");

		report.addFileFromResource(
			"js/main.js", "dependencies/metrics/test-suite-report/");

		report.addFileFromResource("js/utils.js", "dependencies/metrics/");

		JobHistoryDataset jobHistoryDataset =
			JobHistoryDataset.newTestSuiteDataset(
				"test-portal-acceptance-pullrequest(master)", startDateString,
				durationDays);

		report.addFile(
			"js/table-data.js", jobHistoryDataset.getTableDataJSFileContent());

		report.addFile(
			"js/timeline-data.js",
			jobHistoryDataset.getTimelineDataJSFileContent());

		return report;
	}

	public Report(File outputDir) {
		_outputDir = outputDir;
	}

	public void addFile(String fileName, String fileContent) {
		_fileMap.put(new File(_outputDir, fileName), fileContent);
	}

	public void addFileFromResource(String fileName, String resourceDirPath) {
		try {
			addFile(
				fileName,
				JenkinsResultsParserUtil.getResourceFileContent(
					resourceDirPath + fileName));
		}
		catch (IOException ioException) {
			System.out.println(
				"Unable to get file content from resource: " + resourceDirPath +
					fileName);
		}
	}

	public void write() throws IOException {
		FileUtils.deleteDirectory(_outputDir);

		for (Map.Entry<File, String> entry : _fileMap.entrySet()) {
			JenkinsResultsParserUtil.write(entry.getKey(), entry.getValue());
		}
	}

	private final Map<File, String> _fileMap = new HashMap<>();
	private final File _outputDir;

}