/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

'use client';

import Icon from '@clayui/icon';
import {useState} from 'react';

import styles from './sidebar.module.css';

type Props = {
	logo: React.ReactNode;
	children: React.ReactNode;
};

export function SidebarMobile({children, logo}: Props) {
	const [expand, setExpand] = useState(false);

	return (
		<nav className={styles.sidebar_mobile}>
			<div className={styles.sidebar_mobile_header}>
				{logo}

				<button
					aria-label="Toggle navigation"
					className={styles.sidebar_menu_button}
					onClick={() => setExpand(!expand)}
					type="button"
				>
					<Icon spritemap="/images/icons/icons.svg" symbol="bars" />
				</button>
			</div>

			{expand && children}
		</nav>
	);
}
