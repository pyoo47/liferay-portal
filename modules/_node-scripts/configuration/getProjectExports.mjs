/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import projectScopeRequire from '../util/projectScopeRequire.mjs';

/**
 * @returns
 * Something like:
 *
 * [
 *   '@liferay/js-api',
 *   '@liferay/js-api/data-set',
 *   {
 *    moduleName: 'foo',
 *    path: './src/main/resources/META-INF/resources/foo.js'
 *   }
 * ]
 */
export default function getProjectExports() {
	const {exports} = projectScopeRequire('./node-scripts.config.js');

	if (exports === undefined) {
		return [];
	}

	return exports.map((module) => {
		if (typeof module === 'string') {
			return {
				moduleName: module,
				path: module,
			};
		}

		return module;
	});
}
