/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ant.mirrors.get;

import java.lang.reflect.Field;

import java.util.Map;

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
	public void testTryLocalNetworkFalseWhenNoCIEnvVarsSet() throws Exception {
		_removeEnv("JENKINS_URL");
		_removeEnv("MASTER_NETWORK_NAME");

		Assert.assertFalse(_getTryLocalNetwork(new MirrorsGetTask()));
	}

	@Test
	public void testTryLocalNetworkTrueWhenJenkinsURLSet() throws Exception {
		_removeEnv("MASTER_NETWORK_NAME");
		_setEnv("JENKINS_URL", "http://test-1-41.liferay.com");

		Assert.assertTrue(_getTryLocalNetwork(new MirrorsGetTask()));
	}

	@Test
	public void testTryLocalNetworkTrueWhenMasterNetworkNameSet()
		throws Exception {

		_removeEnv("JENKINS_URL");
		_setEnv("MASTER_NETWORK_NAME", "test-network");

		Assert.assertTrue(_getTryLocalNetwork(new MirrorsGetTask()));
	}

	private boolean _getTryLocalNetwork(MirrorsGetTask task) throws Exception {
		Field field = MirrorsGetTask.class.getDeclaredField("_tryLocalNetwork");

		field.setAccessible(true);

		return (boolean)field.get(task);
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> _getWritableEnv() throws Exception {
		Map<String, String> env = System.getenv();

		Field field = env.getClass().getDeclaredField("m");

		field.setAccessible(true);

		return (Map<String, String>)field.get(env);
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
