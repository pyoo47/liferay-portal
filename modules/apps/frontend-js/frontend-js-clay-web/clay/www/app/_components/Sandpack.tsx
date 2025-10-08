/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

'use client';

import {
	Sandpack,
	SandpackCodeEditor,
	SandpackLayout,
	SandpackPreview,
	SandpackProvider,
} from '@codesandbox/sandpack-react';

export const theme = {
	colors: {
		surface1: '#ffffff',
		surface2: '#F3F3F3',
		surface3: '#f5f5f5',
		clickable: '#959da5',
		base: '#24292e',
		disabled: '#d1d4d8',
		hover: '#24292e',
		accent: '#24292e',
	},
	syntax: {
		keyword: '#d73a49',
		property: '#005cc5',
		plain: '#24292e',
		static: '#032f62',
		string: '#032f62',
		definition: '#6f42c1',
		punctuation: '#24292e',
		tag: '#22863a',
		comment: {
			color: '#6a737d',
			fontStyle: 'normal',
		},
	},
	font: {
		body: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol"',
		mono: '"Fira Mono", "DejaVu Sans Mono", Menlo, Consolas, "Liberation Mono", Monaco, "Lucida Console", monospace',
		size: '13px',
		lineHeight: '20px',
	},
};

export {SandpackProvider, SandpackLayout, SandpackCodeEditor, SandpackPreview};
export default Sandpack;
