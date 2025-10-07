/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const fs = require('fs');
const path = require('path');

const clayDir = path.resolve(__dirname, 'clay', 'packages');

const clayJSExports = fs
	.readdirSync(clayDir)
	.filter((dir) => dir.startsWith('clay-') && dir !== 'clay-css')
	.map((dir) => {
		const packageJsonPath = path.join(clayDir, dir, 'package.json');
		const pkg = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));

		return {
			moduleName: pkg.name,
			path: `./clay/packages/${dir}/lib/esm/index.js`,
		};
	});

const clayCSSExports = [
	{
		moduleName: '@clayui/css/lib/css/atlas.css',
		path: './clay/packages/clay-css/lib/css/atlas.css',
	},
	{
		moduleName: '@clayui/css/lib/css/base.css',
		path: './clay/packages/clay-css/lib/css/base.css',
	},
	{
		moduleName: '@clayui/css/lib/css/cadmin.css',
		path: './clay/packages/clay-css/lib/css/cadmin.css',
	},
];

module.exports = {
	exports: [...clayCSSExports, ...clayJSExports],
	symbols: {
		'@clayui/charts': ['bb', 'default'],
	},
};
