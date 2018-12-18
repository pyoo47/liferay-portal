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

package com.liferay.jenkins.results.parser;

import java.io.IOException;
import java.io.InputStream;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Kevin Yen
 */
public class TestrayServerNamesUtilTest {

	@Before
	public void setUp() {
		try (InputStream is =
				TestrayServerNamesUtilTest.class.getResourceAsStream(
					"/testray-server-names-util.properties")) {

			Properties properties = new Properties();

			properties.load(is);

			Stream<Map.Entry<Object, Object>> propertiesStream =
				properties.entrySet().stream();

			_antProperties = new Hashtable<>(
				propertiesStream.collect(
					Collectors.toMap(
						entry -> entry.getKey().toString(),
						Map.Entry::getValue)));
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Test
	public void testGetTestrayServerNames() {
		String testrayServerNames =
			TestrayServerNamesUtil.getTestrayServerNames(
				_antProperties, "test-portal-acceptance-pullrequest(master)",
				"functional-tomcat90-mysql57-jdk8");

		Assert.assertEquals("uat", testrayServerNames);
	}

	@Test
	public void testGetTestrayServerNamesExactMatch() {
		String testrayServerNames =
			TestrayServerNamesUtil.getTestrayServerNames(
				_antProperties, "test-portal-acceptance-pullrequest(master)",
				"integration-mysql57-jdk8");

		Assert.assertEquals("nightly", testrayServerNames);
	}

	@Test
	public void testGetTestrayServerNamesUseDefault() {
		String testrayServerNames =
			TestrayServerNamesUtil.getTestrayServerNames(
				_antProperties, "test-portal-acceptance-pullrequest(master)",
				"js-unit-jdk8");

		Assert.assertEquals("sandbox", testrayServerNames);
	}

	@Test
	public void testGetTestrayServerNamesStartWithMatch() {
		String testrayServerNames =
			TestrayServerNamesUtil.getTestrayServerNames(
				_antProperties, "test-portal-acceptance-pullrequest(ee-6.2.x)",
				"integration-mysql57-jdk8");

		Assert.assertEquals("sandbox", testrayServerNames);
	}

	@Test
	public void testGetTestrayServerNamesNoMatch() {
		String testrayServerNames =
			TestrayServerNamesUtil.getTestrayServerNames(
				_antProperties, "test-plugins-acceptance-pullrequest(ee-6.2.x)",
				"integration-mysql57-jdk8");

		Assert.assertEquals("", testrayServerNames);
	}

	private Hashtable<String, Object> _antProperties;

}