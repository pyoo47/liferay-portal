/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import getFlatName from '../../util/getFlatName.mjs';
import getExportBridgePath from './getExportBridgePath.mjs';

export default function getEntryPoint(projectExport) {
	let entryPoint;

	const {moduleName, path} = projectExport;

	if (moduleName.endsWith('.css')) {
		entryPoint = {
			in: path,
			out: `css/${getFlatName(moduleName).replace(/\.css$/, '')}`,
		};
	}
	else {
		entryPoint = {
			in: getExportBridgePath(moduleName),
			out: `exports/${getFlatName(moduleName)}`,
		};
	}

	return entryPoint;
}
