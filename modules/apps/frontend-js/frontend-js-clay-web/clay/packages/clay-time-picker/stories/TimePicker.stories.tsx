/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

import ClayTimePicker from '../src';

export default {
	component: ClayTimePicker,
	title: 'Design System/Components/TimePicker',
};

export const Default = (args: any) => (
	<div className="sheet">
		<div className="form-group">
			<label>Time Picker</label>

			<ClayTimePicker
				disabled={args.disabled}
				icon={args.showIcon}
				name={args.name}
				timezone={args.timezone}
				use12Hours={args.use12Hours}
			/>
		</div>
	</div>
);

Default.args = {
	disabled: false,
	name: 'time-picker',
	showIcon: false,
	timezone: 'GMT+01:00',
	use12Hours: false,
};
