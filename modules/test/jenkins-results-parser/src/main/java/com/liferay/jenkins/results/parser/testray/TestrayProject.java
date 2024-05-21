/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import java.net.URL;

/**
 * @author Michael Hashimoto
 */
public interface TestrayProject {

	public TestrayProductVersion createTestrayProductVersion(
		String testrayProductVersionName);

	public TestrayRoutine createTestrayRoutine(String testrayRoutineName);

	public String getDescription();

	public long getID();

	public String getName();

	public TestrayProductVersion getTestrayProductVersionByID(
		long productVersionID);

	public TestrayProductVersion getTestrayProductVersionByName(
		String productVersionName);

	public TestrayRoutine getTestrayRoutineByID(long routineID);

	public TestrayRoutine getTestrayRoutineByName(String routineName);

	public TestrayServer getTestrayServer();

	public URL getURL();

}