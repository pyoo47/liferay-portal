/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

'use client';

import classNames from 'classnames';
import Link from 'next/link';
import {usePathname} from 'next/navigation';

import styles from './link.module.css';

export default function ClayLink({
	children,
	href,
	...otherProps
}: React.ComponentProps<typeof Link>) {
	const pathname = usePathname();

	return (
		<Link
			{...otherProps}
			className={classNames({
				[styles.link_active]: pathname === href,
			})}
			href={href}
			rel={
				typeof href === 'string' && href.includes('http')
					? 'noopener noreferrer'
					: undefined
			}
			target={
				typeof href === 'string' && href.includes('http')
					? '_blank'
					: undefined
			}
		>
			{children}
		</Link>
	);
}
