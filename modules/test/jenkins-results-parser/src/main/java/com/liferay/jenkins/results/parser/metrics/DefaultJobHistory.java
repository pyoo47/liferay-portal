/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.metrics;

/**
 * @author Kenji Heigel
 */
public class DefaultJobHistory extends BaseJobHistory {

	public static String getName(String jobName) {
		String name = jobName.replace("-batch", "");

		name = name.replace("-downstream", "");
		name = name.replace("-validation", "");

		return name;
	}

	public DefaultJobHistory(String name, long startTime, long duration) {
		super(getName(name), startTime, duration);
	}

	@Override
	public void addBuildDataJSONObject(
		BuildDataJSONObject buildDataJSONObject) {

		String url = buildDataJSONObject.getURL();

		if ((url != null) && !url.isEmpty()) {
			super.addBuildDataJSONObject(buildDataJSONObject);

			if (!url.contains("-batch") && !url.contains("-downstream") &&
				!url.contains("maintenance") && !url.contains("-validation")) {

				addTopLevelBuildURL(url);
			}
		}
	}

	@Override
	public void initTable() {
		setTable(new DefaultTable());
	}

	protected class DefaultTable extends BaseTable {

		@Override
		public String getFirstColumnEntryName() {
			return getName();
		}

		@Override
		public String getFirstColumnHeader() {
			return "Job Name";
		}

	}

}