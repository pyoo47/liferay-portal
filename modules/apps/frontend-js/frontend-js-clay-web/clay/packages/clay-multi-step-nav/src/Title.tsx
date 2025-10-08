/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

type Props = {

	/**
	 * Title content.
	 */
	children?: React.ReactNode;
};

const Title = ({children}: Props) => (
	<div className="multi-step-title">{children}</div>
);

export default Title;
