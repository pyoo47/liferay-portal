/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

'use client';

import {getSandpackCssText} from '@codesandbox/sandpack-react';
import {useServerInsertedHTML} from 'next/navigation';

/**
 * Ensures CSSinJS styles are loaded server side.
 */
export const SandPackCSS = () => {
	useServerInsertedHTML(() => {
		return (
			<style
				dangerouslySetInnerHTML={{__html: getSandpackCssText()}}
				id="sandpack"
			/>
		);
	});

	return null;
};
