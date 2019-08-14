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
 * @author Michael Hashimoto
 */
public abstract class BaseGitRef implements GitRef {

	@Override
	public LocalGitRepository getLocalGitRepository() {
		return _localGitRepository;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getSHA() {
		return _sha;
	}

	protected BaseGitRef(
		LocalGitRepository localGitRepository, String name, String sha) {

		if (localGitRepository == null) {
			throw new IllegalArgumentException("Local git repository is null");
		}

		if ((name == null) || name.isEmpty()) {
			throw new IllegalArgumentException("Name is null");
		}

		if ((sha == null) || sha.isEmpty()) {
			throw new IllegalArgumentException("SHA is null");
		}

		if (!sha.matches("[0-9a-f]{7,40}")) {
			throw new IllegalArgumentException("SHA is invalid");
		}

		_localGitRepository = localGitRepository;
		_name = name;
		_sha = sha;
	}

	private final LocalGitRepository _localGitRepository;
	private final String _name;
	private final String _sha;

}