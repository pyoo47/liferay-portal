/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

type Props = {
	children: React.ReactNode;
};

export function Sheet({children}: Props) {
	return <div className="sheet-example">{children}</div>;
}
