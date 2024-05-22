/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

/**
 * @author Michael Hashimoto
 */
public interface TestrayCase {

	public String getComponent();

	public String getID();

	public String getName();

	public int getPriority();

	public String getTeamName();

	public TestrayProject getTestrayProject();

	public String getType();

}