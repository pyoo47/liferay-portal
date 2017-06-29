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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Kevin Yen
 */
public class Prerequisites implements Iterable<Prerequisite> {

	public void add(Prerequisite prerequisite) {
		_prerequisites.add(prerequisite);
	}

	public PrerequisiteState getState() {
		PrerequisiteState overallState = PrerequisiteState.INVOKE;

		for (Prerequisite prerequisite : _prerequisites) {
			PrerequisiteState prerequisiteState = prerequisite.getState();

			if (prerequisiteState.equals(PrerequisiteState.DISCARD)) {
				return PrerequisiteState.DISCARD;
			}

			if (prerequisiteState.equals(PrerequisiteState.PENDING)) {
				overallState = PrerequisiteState.PENDING;
			}
		}

		return overallState;
	}

	@Override
	public Iterator<Prerequisite> iterator() {
		return _prerequisites.iterator();
	}

	public int size() {
		return _prerequisites.size();
	}

	private final List<Prerequisite> _prerequisites = new ArrayList<>();

}