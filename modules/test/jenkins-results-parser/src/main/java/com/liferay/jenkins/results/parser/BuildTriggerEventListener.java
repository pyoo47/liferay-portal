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

import java.util.Objects;

/**
 * @author Kevin Yen
 */
public class BuildTriggerEventListener implements BuildEventListener {

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}

		if (!(object instanceof BuildTriggerEventListener)) {
			return false;
		}

		BuildTriggerEventListener buildTriggerEventListener =
			(BuildTriggerEventListener)object;

		if (Objects.equals(_build, buildTriggerEventListener._build)) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_build);
	}

	@Override
	public void onBuildEvent(BuildEvent buildEvent) {
		_build.evaluate();
	}

	protected BuildTriggerEventListener(Build build) {
		_build = build;
	}

	private final Build _build;

}