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

import com.liferay.poshi.runner.util.PropsValues;

import java.util.Properties;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Hashimoto
 */
public class PQLObjectValueTest extends TestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		System.out.println("##\n## setup\n##");
	}

	@After
	@Override
	public void tearDown() throws Exception {
		System.out.println("##\n## teardown\n##");
	}

	@Test
	public void testQuery() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("portal.acceptance", "true");
		properties.setProperty("portal.acceptance.tomcat.mysql", "true");
		properties.setProperty("testray.component.names", "Blogs Search,WCM");

		String query = "(portal.acceptance=\"true\" OR (portal.acceptance.tomcat.mysql=\"false\" OR portal = \"true\")) AND (testray.component.names~\"Blogs Search\" OR testray.component.names!~Blah)";

		PQLQuery pqlQuery = new PQLQuery(query, properties);

		System.out.println(properties);
		System.out.println(query);
		System.out.println(pqlQuery.getResult());
	}

	@Test
	public void testQuery2() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("portal", "true");

		String query =
			"portal.acceptance ~ \"true\" OR portal.acceptance !~ true";

		PQLQuery pqlQuery = new PQLQuery(query, properties);

		System.out.println(properties);
		System.out.println(query);
		System.out.println(pqlQuery.getResult());
	}

}