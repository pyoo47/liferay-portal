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

package com.liferay.portal.kernel.language;

import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.Rule;
import org.junit.rules.TestRule;
import com.liferay.portal.kernel.test.rule.TimeoutTestRule;


/**
 * @author Drew Brokke
 */
public class LanguagePropertyTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
		List<String> modulesFileNames = _getFileNames(
			"./modules/**/src/**/Language*.properties");
		List<String> portalImplFileNames = _getFileNames(
			"./portal-impl/src/**/Language*.properties");

		_modulesPropertiesMap = _getPropertiesMap(modulesFileNames);
		_portalImplPropertiesMap = _getPropertiesMap(portalImplFileNames);
	}

	@Rule
	public final TestRule testRule = TimeoutTestRule.INSTANCE;

	@Test
	public void testSpecialKeyDir() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testSpecialKey(LanguageConstants.KEY_DIR);
	}

	@Test
	public void testSpecialKeyLineBegin() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testSpecialKey(LanguageConstants.KEY_LINE_BEGIN);
	}

	@Test
	public void testSpecialKeyLineEnd() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testSpecialKey(LanguageConstants.KEY_LINE_END);
	}

	@Test
	public void testSpecialKeyUserNameFieldNames() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testSpecialKey(LanguageConstants.KEY_USER_NAME_FIELD_NAMES);
	}

	@Test
	public void testSpecialKeyUserNamePrefixValues() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testSpecialKey(LanguageConstants.KEY_USER_NAME_PREFIX_VALUES);
	}

	@Test
	public void testSpecialKeyUserNameRequiredFieldNames() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testSpecialKey(LanguageConstants.KEY_USER_NAME_REQUIRED_FIELD_NAMES);
	}

	@Test
	public void testSpecialKeyUserNameSuffixValues() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testSpecialKey(LanguageConstants.KEY_USER_NAME_SUFFIX_VALUES);
	}

	@Test
	public void testUserNameRequiredFieldNamesSubsetOfUserNameFieldNames() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		Set<String> paths = _portalImplPropertiesMap.keySet();

		List<String> failureMessages = new ArrayList<>();

		for (String path : paths) {
			Properties properties = _portalImplPropertiesMap.get(path);

			String userNameFieldNamesString = properties.getProperty(
				LanguageConstants.KEY_USER_NAME_FIELD_NAMES);
			String userNameRequiredFieldNamesString = properties.getProperty(
				LanguageConstants.KEY_USER_NAME_REQUIRED_FIELD_NAMES);

			if ((userNameFieldNamesString == null) ||
				(userNameRequiredFieldNamesString == null)) {

				continue;
			}

			String[] userNameFieldNames = StringUtil.split(
				userNameFieldNamesString);
			String[] userNameRequiredFieldNames = StringUtil.split(
				userNameRequiredFieldNamesString);

			for (String userNameRequiredFieldName :
					userNameRequiredFieldNames) {

				if (!ArrayUtil.contains(
						userNameFieldNames, userNameRequiredFieldName)) {

					failureMessages.add(path);
				}
			}
		}

		if (!failureMessages.isEmpty()) {
			Assert.fail(
				"Required field names are not a subset of user name field " +
					"names in " + failureMessages);
		}
	}

	@Test
	public void testValidKeyDir() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testValidKey(LanguageConstants.KEY_DIR);
	}

	@Test
	public void testValidKeyLineBegin() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testValidKey(LanguageConstants.KEY_LINE_BEGIN);
	}

	@Test
	public void testValidKeyLineEnd() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testValidKey(LanguageConstants.KEY_LINE_END);
	}

	@Test
	public void testValidKeyUserNameFieldNames() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testValidKey(LanguageConstants.KEY_USER_NAME_FIELD_NAMES);
	}

	@Test
	public void testValidKeyUserNamePrefixValues() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testValidKey(LanguageConstants.KEY_USER_NAME_PREFIX_VALUES);
	}

	@Test
	public void testValidKeyUserNameRequiredFieldNames() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testValidKey(LanguageConstants.KEY_USER_NAME_REQUIRED_FIELD_NAMES);
	}

	@Test
	public void testValidKeyUserNameSuffixValues() {
		try {
			Thread.sleep(400000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		_testValidKey(LanguageConstants.KEY_USER_NAME_SUFFIX_VALUES);
	}

	private static List<String> _getFileNames(String pattern)
		throws IOException {

		final List<String> fileNames = new ArrayList<>();

		FileSystem fileSystem = FileSystems.getDefault();

		final PathMatcher pathMatcher = fileSystem.getPathMatcher(
			"glob:" + pattern);

		FileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(
				Path path, BasicFileAttributes basicFileAttributes) {

				if (pathMatcher.matches(path)) {
					fileNames.add(path.toString());
				}

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(
				Path path, IOException ioeException) {

				return FileVisitResult.CONTINUE;
			}

		};

		Files.walkFileTree(Paths.get("./"), simpleFileVisitor);

		return fileNames;
	}

	private static Map<String, Properties> _getPropertiesMap(
			List<String> fileNames)
		throws IOException {

		Map<String, Properties> propertiesMap = new HashMap<>();

		for (String fileName : fileNames) {
			Properties properties = new Properties();

			try (InputStream inputStream = new FileInputStream(fileName)) {
				properties.load(inputStream);
			}

			propertiesMap.put(fileName, properties);
		}

		return propertiesMap;
	}

	private void _testSpecialKey(String key) {
		List<String> invalidFileNames = new ArrayList<>();

		Set<String> fileNames = _modulesPropertiesMap.keySet();

		for (String fileName : fileNames) {
			Properties properties = _modulesPropertiesMap.get(fileName);

			Set<String> propertyNames = properties.stringPropertyNames();

			if (propertyNames.contains(key)) {
				invalidFileNames.add(fileName);
			}
		}

		if (!invalidFileNames.isEmpty()) {
			Assert.fail(
				"Special key \"" + key + "\" is found in: " + invalidFileNames);
		}
	}

	private void _testValidKey(String key) {
		List<String> invalidFileNames = new ArrayList<>();

		Set<String> fileNames = _portalImplPropertiesMap.keySet();

		for (String path : fileNames) {
			Properties properties = _portalImplPropertiesMap.get(path);

			String value = properties.getProperty(key);

			if ((value != null) && !LanguageValidator.isValid(key, value)) {
				invalidFileNames.add(path);
			}
		}

		if (!invalidFileNames.isEmpty()) {
			Assert.fail(
				"Invalid values for key \"" + key + "\" are found in: " +
					invalidFileNames);
		}
	}

	private static Map<String, Properties> _modulesPropertiesMap;
	private static Map<String, Properties> _portalImplPropertiesMap;

}