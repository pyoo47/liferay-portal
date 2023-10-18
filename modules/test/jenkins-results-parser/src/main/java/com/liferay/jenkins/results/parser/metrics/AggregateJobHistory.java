/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.metrics;

/**
 * @author Kenji Heigel
 */
public class AggregateJobHistory extends BaseJobHistory {

	public static String getCategoryName(String jobName) {
		jobName = jobName.replace("-batch", "");
		jobName = jobName.replace("-downstream", "");
		jobName = jobName.replace("-validation", "");

		if (jobName.contains("maintenance-") || jobName.contains("mirrors-") ||
			jobName.contains("verification-")) {

			return Category.MAINTENANCE.toString();
		}

		if (jobName.equals("test-portal-acceptance-pullrequest(master)")) {
			return Category.PORTAL_MASTER_PULLREQUEST.toString();
		}

		if (jobName.equals("test-portal-acceptance-upstream(master)") ||
			jobName.equals("test-portal-acceptance-upstream-dxp(master)") ||
			jobName.equals("test-portal-testsuite-upstream(master)")) {

			return Category.PORTAL_MASTER_UPSTREAM.toString();
		}

		if (jobName.equals("test-portal-release")) {
			return Category.PORTAL_RELEASE.toString();
		}

		if (jobName.equals("test-portal-fixpack-release") ||
			jobName.equals("test-portal-hotfix-release")) {

			return Category.PORTAL_OTHER_RELEASE.toString();
		}

		if (jobName.contains("test-portal-")) {
			return Category.PORTAL_OTHER.toString();
		}

		return Category.OTHER.toString();
	}

	public AggregateJobHistory(String name, long startTime, long duration) {
		super(name, startTime, duration);
	}

	public void addJobHistory(JobHistory jobHistory) {
		addBuildDataJSONObjects(jobHistory.getBuildDataJSONObjects());
		addTopLevelBuildURLs(jobHistory.getTopLevelBuildURLs());
	}

	@Override
	public void initTable() {
		setTable(new AggregateTable());
	}

	protected class AggregateTable extends BaseTable {

		@Override
		public String getFirstColumnEntryName() {
			return getName();
		}

		@Override
		public String getFirstColumnHeader() {
			return "CI Job Category";
		}

	}

	private enum Category {

		MAINTENANCE("CI Maintenance"), OTHER("Other"),
		PORTAL_MASTER_PULLREQUEST("liferay-portal/master PR's"),
		PORTAL_MASTER_UPSTREAM("liferay-portal/master Upstream"),
		PORTAL_OTHER("liferay-portal-ee PR's & Upstream"),
		PORTAL_OTHER_RELEASE("Portal Fixpack & Hotfix Release"),
		PORTAL_RELEASE("Portal Release");

		@Override
		public String toString() {
			return _string;
		}

		private Category(String string) {
			_string = string;
		}

		private final String _string;

	}

}