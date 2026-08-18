/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ant.mirrors.get.internal;

import java.io.File;
import java.io.IOException;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Calum Ragan
 */
public class MirrorsCacheLinker {

	public static final long MAX_AGE_MILLIS = 24 * 60 * 60 * 1000;

	public static File createReadLink(File cacheFile, File linkFile) {
		Path cacheFilePath = cacheFile.toPath();
		Path linkFilePath = linkFile.toPath();

		try {
			Files.createLink(linkFilePath, cacheFilePath);

			return linkFile;
		}
		catch (NoSuchFileException noSuchFileException) {
			return null;
		}
		catch (IOException | UnsupportedOperationException exception) {
			if (cacheFile.exists()) {
				return cacheFile;
			}

			return null;
		}
	}

	public static boolean isSameFileKey(File file1, File file2) {
		Object fileKey1 = _getFileKey(file1);

		if (fileKey1 == null) {
			return false;
		}

		Object fileKey2 = _getFileKey(file2);

		if (fileKey2 == null) {
			return false;
		}

		return fileKey1.equals(fileKey2);
	}

	public static boolean publish(File tempFile, File cacheFile)
		throws IOException {

		Path cacheFilePath = cacheFile.toPath();
		Path tempFilePath = tempFile.toPath();

		try {
			Files.createLink(cacheFilePath, tempFilePath);

			return true;
		}
		catch (FileAlreadyExistsException fileAlreadyExistsException) {
			return false;
		}
		catch (IOException | UnsupportedOperationException exception) {
			if (cacheFile.exists()) {
				return false;
			}

			return _publishByRename(tempFile, cacheFile);
		}
	}

	public static void sweep(File cacheFile) {
		File parentFile = cacheFile.getParentFile();

		if (parentFile == null) {
			return;
		}

		File[] files = parentFile.listFiles();

		if (files == null) {
			return;
		}

		long oldestTime = System.currentTimeMillis() - MAX_AGE_MILLIS;
		Pattern pattern = _getOrphanPattern(cacheFile.getName());

		for (File file : files) {
			Matcher matcher = pattern.matcher(file.getName());

			if (!matcher.matches()) {
				continue;
			}

			long time = Long.parseLong(matcher.group("timestamp"));

			if (time > oldestTime) {
				continue;
			}

			file.delete();
		}
	}

	public static String uniqueLinkFileName(String fileName) {
		return _uniquePrefix() + "link-" + fileName;
	}

	public static String uniqueTempFileName(String fileName) {
		return _uniquePrefix() + fileName;
	}

	private static Object _getFileKey(File file) {
		try {
			BasicFileAttributes basicFileAttributes = Files.readAttributes(
				file.toPath(), BasicFileAttributes.class);

			return basicFileAttributes.fileKey();
		}
		catch (IOException ioException) {
			return null;
		}
	}

	private static Pattern _getOrphanPattern(String fileName) {
		StringBuilder sb = new StringBuilder();

		sb.append("(?<timestamp>\\d{13,18})");
		sb.append("(-[0-9a-fA-F-]{36}(-link)?-)?");
		sb.append(Pattern.quote(fileName));

		return Pattern.compile(sb.toString());
	}

	private static boolean _publishByRename(File tempFile, File cacheFile)
		throws IOException {

		if (tempFile.renameTo(cacheFile)) {
			return true;
		}

		if (cacheFile.exists()) {
			return false;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("Unable to publish ");
		sb.append(tempFile.getPath());
		sb.append(" to ");
		sb.append(cacheFile.getPath());
		sb.append(".");

		throw new IOException(sb.toString());
	}

	private static String _uniquePrefix() {
		StringBuilder sb = new StringBuilder();

		sb.append(System.currentTimeMillis());
		sb.append("-");
		sb.append(UUID.randomUUID());
		sb.append("-");

		return sb.toString();
	}

}