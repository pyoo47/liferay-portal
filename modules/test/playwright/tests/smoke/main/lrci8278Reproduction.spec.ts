/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, test} from '@playwright/test';

test('LRCI-8278 deliberate failure to exercise the Testray import', async () => {
	expect('LRCI-8278 throwaway reproduction, safe to delete').toBe(
		'this assertion is expected to fail'
	);
});
