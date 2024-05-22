/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import java.util.List;

/**
 * @author Michael Hashimoto
 */
public interface TestrayRun {

	public String getBatchName();

	public List<Factor> getFactors();

	public String getRunID();

	public String getRunIDString();

	public TestrayBuild getTestrayBuild();

	public static class Factor {

		public Factor(String name, String value) {
			_name = name;
			_value = value;
		}

		public String getName() {
			return _name;
		}

		public String getValue() {
			return _value;
		}

		@Override
		public String toString() {
			return getName() + "=" + getValue();
		}

		private final String _name;
		private final String _value;

	}

}