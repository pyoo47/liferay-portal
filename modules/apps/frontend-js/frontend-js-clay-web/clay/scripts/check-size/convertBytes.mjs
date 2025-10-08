/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const SIZES = ['B', 'KB', 'MB', 'GB', 'TB'];

export const convertBytes = function (bytes) {
	if (bytes === 0) {
		return '--';
	}

	bytes = Math.abs(bytes);

	const i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)), 10);

	if (i === 0) {
		return `${bytes} ${SIZES[i]}`;
	}

	return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${SIZES[i]}`;
};
