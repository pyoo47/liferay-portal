/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import classNames from 'classnames';
import {promises as fs} from 'fs';

import {
	SandpackCodeEditor,
	SandpackLayout,
	SandpackPreview,
	SandpackProvider,
	theme,
} from './Sandpack';
import styles from './sandpack.module.css';

type Props = {
	language: string;
	children: any;
};

export async function Sandpack({children, language}: Props) {
	const file = await fs.readFile(
		process.cwd() + '/node_modules/@clayui/css/lib/images/icons/icons.svg',
		'utf-8'
	);

	return (
		<SandpackProvider
			className={classNames('mb-4', styles.code_editor_open)}
			customSetup={{
				dependencies: {
					'react': '^16.12.0',
					'react-dom': '^16.12.0',
					'@clayui/core': 'latest',
					'@clayui/icon': 'latest',
					'@clayui/button': 'latest',
					'@clayui/css': 'latest',
					'@clayui/form': 'latest',
					'@clayui/shared': 'latest',
					'@clayui/layout': 'latest',
					'@clayui/drop-down': 'latest',
					'@clayui/label': 'latest',
					'@clayui/alert': 'latest',
					'@clayui/autocomplete': 'latest',
					'@clayui/badge': 'latest',
					'@clayui/breadcrumb': 'latest',
					'@clayui/card': 'latest',
					'@clayui/color-picker': 'latest',
					'@clayui/date-picker': 'latest',
					'@clayui/empty-state': 'latest',
					'@clayui/link': 'latest',
					'@clayui/list': 'latest',
					'@clayui/loading-indicator': 'latest',
					'@clayui/localized-input': 'latest',
					'@clayui/management-toolbar': 'latest',
					'@clayui/modal': 'latest',
					'@clayui/multi-select': 'latest',
					'@clayui/sticker': 'latest',
					'@clayui/multi-step-nav': 'latest',
					'@clayui/nav': 'latest',
					'@clayui/navigation-bar': 'latest',
					'@clayui/pagination': 'latest',
					'@clayui/pagination-bar': 'latest',
					'@clayui/panel': 'latest',
					'@clayui/popover': 'latest',
					'@clayui/progress-bar': 'latest',
					'@clayui/tabs': 'latest',
					'@clayui/time-picker': 'latest',
					'@clayui/toolbar': 'latest',
					'@clayui/tooltip': 'latest',
					'@clayui/upper-toolbar': 'latest',
				},
			}}
			files={{
				'/public/icons.svg': file,
				'/App.js': {
					active: true,
					code: children,
				},
				'/index.js': {
					code: `import React, { StrictMode } from "react";
import ReactDOM from "react-dom";
import "./styles.css";

import App from "./App";

ReactDOM.render(
  <StrictMode>
    <App />
  </StrictMode>,
  document.getElementById("root")
);`,
				},
			}}
			options={{
				experimental_enableServiceWorker: true,
			}}
			template={language === 'jsx' ? 'react' : undefined}
			theme={theme as any}
		>
			<div className={styles.code_preview}>
				<SandpackPreview />
			</div>

			<SandpackLayout>
				<SandpackCodeEditor showLineNumbers showTabs={false} />
			</SandpackLayout>
		</SandpackProvider>
	);
}
