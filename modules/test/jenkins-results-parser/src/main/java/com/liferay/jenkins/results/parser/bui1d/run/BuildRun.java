/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.bui1d.run;

import com.liferay.jenkins.results.parser.Build;
import com.liferay.jenkins.results.parser.JenkinsCohort;
import com.liferay.jenkins.results.parser.JenkinsMaster;

import java.net.URL;

/**
 * @author Michael Hashimoto
 */
public interface BuildRun {

	public Build getBuild();

	public int getBuildNumber();

	public URL getBuildURL();

	public int getInvokedBatchSize();

	public JenkinsCohort getJenkinsCohort();

	public JenkinsMaster getJenkinsMaster();

	public long getJenkinsQueueId();

	public int getMaximumSlavesPerHost();

	public int getMinimumSlaveRAM();

	public Status getStatus();

	public void invoke();

	public void setBuildNumber(int buildNumber);

	public void setInvokedBatchSize(int invokedBatchSize);

	public void setJenkinsCohort(JenkinsCohort jenkinsCohort);

	public void setJenkinsMaster(JenkinsMaster jenkinsMaster);

	public void setJenkinsQueueId(long queueId);

	public void setMaximumSlavesPerHost(int maximumSlavesPerHost);

	public void setMinimumSlaveRAM(int minimumSlaveRAM);

	public void setStatus(Status status);

	public void update();

	public enum Status {

		COMPLETED("completed"), MISSING("missing"), QUEUED("queued"),
		REPORTING("reporting"), RUNNING("running"), STARTING("starting");

		public String getKey() {
			return _key;
		}

		private Status(String key) {
			_key = key;
		}

		private final String _key;

	}

}