/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Peter Yoo
 */
public class LRCI8278ReproductionTest {

	@Test
	public void testReproduction() {
		Assert.fail(
			"LRCI-8278 throwaway reproduction, safe to delete: " +
				"java.lang.reflect.InvocationTargetException");
	}

}
