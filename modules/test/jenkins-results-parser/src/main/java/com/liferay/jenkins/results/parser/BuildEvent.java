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

/**
 * @author Kevin Yen
 */
public class BuildEvent {

	public BuildEvent(Build build, String newStatus, String oldStatus) {
		_build = build;
		_newStatus = newStatus;
		_oldStatus = oldStatus;

		if (JenkinsResultsParserUtil.debug) {
			System.out.println(toString());
		}
	}

	public Build getBuild() {
		return _build;
	}

	public String getNewStatus() {
		return _newStatus;
	}

	public String getOldStatus() {
		return _oldStatus;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Build event (");
		sb.append(getOldStatus());
		sb.append(" > ");
		sb.append(getNewStatus());
		sb.append(") ");

		if (_build.getBuildURL() != null) {
			try {
				sb.append(
					JenkinsResultsParserUtil.encode(_build.getBuildURL()));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return sb.toString();
	}

	private final Build _build;
	private final String _newStatus;
	private final String _oldStatus;

}