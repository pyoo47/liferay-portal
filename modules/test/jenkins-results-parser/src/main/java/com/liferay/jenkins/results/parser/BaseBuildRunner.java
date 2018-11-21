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
import java.io.IOException;

import java.util.concurrent.TimeoutException;

/**
 * @author Michael Hashimoto
 */
public abstract class BaseBuildRunner<T extends BuildData, S extends Workspace>
	implements BuildRunner<T, S> {

	@Override
	public T getBuildData() {
		return _buildData;
	}

	@Override
	public S getWorkspace() {
		return _workspace;
	}

	@Override
	public void run() {
		updateBuildDescription();

		setUpWorkspace();
	}

	@Override
	public void setUp() {
	}

	@Override
	public void tearDown() {
		cleanUpDatabaseProcesses();

		cleanUpBrowserProcesses();

		cleanUpJavaProcesses();

		tearDownWorkspace();
	}

	protected BaseBuildRunner(T buildData) {
		_buildData = buildData;

		_job = JobFactory.newJob(_buildData);

		_job.readJobProperties();
	}

	protected void cleanUpBrowserProcesses() {
		Host host = _buildData.getHost();

		if (host.hasChrome()) {
			try {
				JenkinsResultsParserUtil.executeBashCommands("killall chrome");
			}
			catch (IOException | TimeoutException e) {
				throw new RuntimeException(e);
			}
		}

		if (host.hasFirefox()) {
			try {
				JenkinsResultsParserUtil.executeBashCommands("killall firefox");
			}
			catch (IOException | TimeoutException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void cleanUpDatabaseProcesses() {
		Host host = _buildData.getHost();

		if (host.hasDB2()) {
			try {
				JenkinsResultsParserUtil.executeBashCommands(
					"db2 db2stop force");
			}
			catch (IOException | TimeoutException e) {
				throw new RuntimeException(e);
			}
		}

		if (host.hasMariaDB()) {
			try {
				JenkinsResultsParserUtil.executeBashCommands(
					"service mariadb stop");
			}
			catch (IOException | TimeoutException e) {
				throw new RuntimeException(e);
			}
		}

		if (host.hasMySQL55()) {
			try {
				JenkinsResultsParserUtil.executeBashCommands(
					"service mysql55 stop");
			}
			catch (IOException | TimeoutException e) {
				throw new RuntimeException(e);
			}
		}

		if (host.hasMySQL56()) {
			try {
				JenkinsResultsParserUtil.executeBashCommands(
					"service mysqld stop");
			}
			catch (IOException | TimeoutException e) {
				throw new RuntimeException(e);
			}
		}

		if (host.hasMySQL57()) {
			try {
				JenkinsResultsParserUtil.executeBashCommands(
					"service mysql57 stop");
			}
			catch (IOException | TimeoutException e) {
				throw new RuntimeException(e);
			}
		}

		if (host.hasOracle()) {
			try {
				JenkinsResultsParserUtil.executeBashCommands(
					"service oracledb stop");
			}
			catch (IOException | TimeoutException e) {
				throw new RuntimeException(e);
			}
		}

		if (host.hasPostgreSQL()) {
			StringBuilder sb = new StringBuilder();

			sb.append("#!/bin/bash\n");
			sb.append("for i in `ls /var/lib/pgsql/`\n");
			sb.append("do\n");
			sb.append("service postgresql-$i stop\n");
			sb.append("done");

			File bashFile = new File("clean_up_postgresql.sh");

			try {
				JenkinsResultsParserUtil.write(bashFile, sb.toString());

				JenkinsResultsParserUtil.executeBashFile(bashFile);
			}
			catch (IOException | TimeoutException e) {
				throw new RuntimeException(e);
			}
			finally {
				bashFile.delete();
			}
		}

		if (host.hasSybase()) {
			StringBuilder sb = new StringBuilder();

			sb.append("#!/bin/bash\n");
			sb.append("service sybase stop");
			sb.append("for i in `ps -o pid,args -e | grep /opt/sybase | ");
			sb.append("grep -v grep | cut -c1-5`\n");
			sb.append("do\n");
			sb.append("kill -9 $i\n");
			sb.append("done");

			File bashFile = new File("clean_up_sybase.sh");

			try {
				JenkinsResultsParserUtil.write(bashFile, sb.toString());

				JenkinsResultsParserUtil.executeBashFile(bashFile);
			}
			catch (IOException | TimeoutException e) {
				throw new RuntimeException(e);
			}
			finally {
				bashFile.delete();
			}
		}
	}

	protected void cleanUpJavaProcesses() {
		StringBuilder sb = new StringBuilder();

		sb.append("#!/bin/bash\n");
		sb.append("for i in `ps -o pid,args -e | grep java | grep -v ant.home");
		sb.append(" | grep -v grep | grep -v jenkins | cut -c1-5`\n");
		sb.append("do\n");
		sb.append("echo \"Killing $i.\"\n");
		sb.append("kill -9 $i\n");
		sb.append("done");

		File bashFile = new File("clean_up_java.sh");

		try {
			JenkinsResultsParserUtil.write(bashFile, sb.toString());

			JenkinsResultsParserUtil.executeBashFile(bashFile);
		}
		catch (IOException | TimeoutException e) {
			throw new RuntimeException(e);
		}
		finally {
			bashFile.delete();
		}
	}

	protected Job getJob() {
		return _job;
	}

	protected abstract void initWorkspace();

	protected void publishToUserContentDir(File file) {
		if (!JenkinsResultsParserUtil.isCINode()) {
			return;
		}

		String userContentRelativePath =
			_buildData.getUserContentRelativePath();

		userContentRelativePath = userContentRelativePath.replace(")", "\\)");
		userContentRelativePath = userContentRelativePath.replace("(", "\\(");

		RemoteExecutor remoteExecutor = new RemoteExecutor();

		int returnCode = remoteExecutor.execute(
			1, new String[] {_buildData.getMasterHostname()},
			new String[] {
				"mkdir -p /opt/java/jenkins/userContent/" +
					userContentRelativePath
			});

		if (returnCode != 0) {
			throw new RuntimeException("Unable to create target directory");
		}

		int maxRetries = 3;
		int retries = 0;

		while (retries < maxRetries) {
			try {
				retries++;

				String command = JenkinsResultsParserUtil.combine(
					"time rsync -Ipqrs --chmod=go=rx --timeout=1200 ",
					file.getCanonicalPath(), " ",
					_buildData.getTopLevelMasterHostname(), "::usercontent/",
					userContentRelativePath);

				JenkinsResultsParserUtil.executeBashCommands(command);

				break;
			}
			catch (IOException | TimeoutException e) {
				if (retries == maxRetries) {
					throw new RuntimeException(
						"Unable to send " + file.getName(), e);
				}

				System.out.println(
					"Unable to execute bash commands, retrying... ");

				e.printStackTrace();

				JenkinsResultsParserUtil.sleep(3000);
			}
		}
	}

	protected void setUpWorkspace() {
		if (_workspace == null) {
			initWorkspace();
		}

		_workspace.setBuildData(getBuildData());

		_workspace.setJob(getJob());

		_workspace.setUp();
	}

	protected void setWorkspace(S workspace) {
		_workspace = workspace;
	}

	protected void tearDownWorkspace() {
		if (_workspace == null) {
			initWorkspace();
		}

		_workspace.tearDown();
	}

	protected void updateBuildDescription() {
		String buildDescription = _buildData.getBuildDescription();

		buildDescription = buildDescription.replaceAll("\"", "\\\\\"");
		buildDescription = buildDescription.replaceAll("\'", "\\\\\'");

		StringBuilder sb = new StringBuilder();

		sb.append("def job = Jenkins.instance.getItemByFullName(\"");
		sb.append(_buildData.getJobName());
		sb.append("\"); ");

		sb.append("def build = job.getBuildByNumber(");
		sb.append(_buildData.getBuildNumber());
		sb.append("); ");

		sb.append("build.description = \"");
		sb.append(buildDescription);
		sb.append("\";");

		JenkinsResultsParserUtil.executeJenkinsScript(
			_buildData.getMasterHostname(), "script=" + sb.toString());
	}

	private final T _buildData;
	private final Job _job;
	private S _workspace;

}