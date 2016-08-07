/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.poshi.runner.pql;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Michael Hashimoto
 */
public class PQLEntityTest extends TestCase {

	@Test
	public void testFixQuery() throws Exception {
		_compare(PQLEntity.fixQuery("(((test)))"), "test");
		_compare(PQLEntity.fixQuery(" (((test)) )"), "test");
		_compare(PQLEntity.fixQuery(" ((( test test ))) "), "test test");
		_compare(PQLEntity.fixQuery(")test("), ")test(");
		_compare(PQLEntity.fixQuery(" )test( "), ")test(");
		_compare(PQLEntity.fixQuery(" ) test"), ") test");
		_compare(PQLEntity.fixQuery(" ( test"), "( test");
		_compare(PQLEntity.fixQuery("test ) "), "test )");
		_compare(PQLEntity.fixQuery("test ( "), "test (");
		_compare(PQLEntity.fixQuery("( (test) OR (test))"), "(test) OR (test)");
	}

	private void _compare(String actualValue, String expectedValue)
		throws Exception {

		if (!actualValue.equals(expectedValue)) {
			throw new Exception(
				"'" + expectedValue + "' should equal '" + actualValue + "'");
		}
	}

}