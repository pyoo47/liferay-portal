/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ant.mirrors.get;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.lang.reflect.Field;

import java.nio.file.Files;

import java.util.Map;

import org.apache.tools.ant.Project;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Charlotte Wong
 */
public class MirrorsGetTaskTest {

	@Before
	public void setUp() {
		_originalJenkinsURL = System.getenv("JENKINS_URL");
		_originalMasterNetworkName = System.getenv("MASTER_NETWORK_NAME");
	}

	@After
	public void tearDown() throws Exception {
		_restoreEnv("JENKINS_URL", _originalJenkinsURL);
		_restoreEnv("MASTER_NETWORK_NAME", _originalMasterNetworkName);
	}

	@Test
	public void testMirrorConnectionAttemptedWhenJenkinsURLSet()
		throws Exception {

		_removeEnv("MASTER_NETWORK_NAME");
		_setEnv("JENKINS_URL", "http://test-1-41.liferay.com");

		Project antProject = new Project();

		antProject.setProperty("mirrors.hostname", "mirrors.lax.liferay.com");

		MirrorsGetTask task = new MirrorsGetTask();

		task.setProject(antProject);
		task.setTaskName("mirrors-get");
		task.setVerbose(true);
		task.setSrc(
			"https://repository.liferay.com/nexus/content/groups/public" +
				"/nonexistent-lrci-7262-test/file.jar");
		task.setDest(
			Files.createTempDirectory(
				"mirrors-get-test"
			).toFile());

		ByteArrayOutputStream capturedByteArrayOutputStream =
			new ByteArrayOutputStream();
		PrintStream originalOut = System.out;

		System.setOut(new PrintStream(capturedByteArrayOutputStream));

		try {
			task.execute();
		}
		catch (Exception exception) {
		}
		finally {
			System.setOut(originalOut);
		}

		String output = capturedByteArrayOutputStream.toString();

		Assert.assertTrue(
			"Mirror URL was not attempted with JENKINS_URL set — output: " +
				output,
			output.contains("mirrors.lax.liferay.com"));
	}

	@Test
	public void testNoMirrorConnectionsAttemptedOutsideCI() throws Exception {
		_removeEnv("JENKINS_URL");
		_removeEnv("MASTER_NETWORK_NAME");

		Project antProject = new Project();

		antProject.setProperty("mirrors.hostname", "mirrors.lax.liferay.com");

		MirrorsGetTask task = new MirrorsGetTask();

		task.setProject(antProject);
		task.setTaskName("mirrors-get");
		task.setVerbose(true);
		task.setSrc(
			"https://repository.liferay.com/nexus/content/groups/public" +
				"/nonexistent-lrci-7262-test/file.jar");
		task.setDest(
			Files.createTempDirectory(
				"mirrors-get-test"
			).toFile());

		ByteArrayOutputStream capturedByteArrayOutputStream =
			new ByteArrayOutputStream();
		PrintStream originalOut = System.out;

		System.setOut(new PrintStream(capturedByteArrayOutputStream));

		try {
			task.execute();
		}
		catch (Exception exception) {
		}
		finally {
			System.setOut(originalOut);
		}

		String output = capturedByteArrayOutputStream.toString();

		Assert.assertFalse(
			"Mirror URL was attempted outside CI — output: " + output,
			output.contains("mirrors.lax.liferay.com"));
	}

	@Test
	public void testTryLocalNetworkFalseWhenNoCIEnvVarsSet() throws Exception {
		_removeEnv("JENKINS_URL");
		_removeEnv("MASTER_NETWORK_NAME");

		Assert.assertFalse(_isTryLocalNetwork(new MirrorsGetTask()));
	}

	@Test
	public void testTryLocalNetworkTrueWhenJenkinsURLSet() throws Exception {
		_removeEnv("MASTER_NETWORK_NAME");
		_setEnv("JENKINS_URL", "http://test-1-41.liferay.com");

		Assert.assertTrue(_isTryLocalNetwork(new MirrorsGetTask()));
	}

	@Test
	public void testTryLocalNetworkTrueWhenMasterNetworkNameSet()
		throws Exception {

		_removeEnv("JENKINS_URL");
		_setEnv("MASTER_NETWORK_NAME", "test-network");

		Assert.assertTrue(_isTryLocalNetwork(new MirrorsGetTask()));
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> _getWritableEnv() throws Exception {
		Map<String, String> env = System.getenv();

		Field field = env.getClass(
		).getDeclaredField(
			"m"
		);

		field.setAccessible(true);

		return (Map<String, String>)field.get(env);
	}

	private boolean _isTryLocalNetwork(MirrorsGetTask task) throws Exception {
		Field field = MirrorsGetTask.class.getDeclaredField("_tryLocalNetwork");

		field.setAccessible(true);

		return (boolean)field.get(task);
	}

	private void _removeEnv(String name) throws Exception {
		_getWritableEnv().remove(name);
	}

	private void _restoreEnv(String name, String value) throws Exception {
		if (value == null) {
			_removeEnv(name);
		}
		else {
			_setEnv(name, value);
		}
	}

	private void _setEnv(String name, String value) throws Exception {
		_getWritableEnv().put(name, value);
	}

	private String _originalJenkinsURL;
	private String _originalMasterNetworkName;

}