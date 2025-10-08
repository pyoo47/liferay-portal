/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import Button, {ButtonProps} from '@clayui/button';
import classNames from 'classnames';
import React from 'react';

export const Trigger = ({children, className, ...otherProps}: ButtonProps) => (
	<Button
		className={classNames(className, 'menubar-toggler')}
		displayType="unstyled"
		{...otherProps}
	>
		{children}
	</Button>
);
