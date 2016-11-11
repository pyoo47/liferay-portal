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

package com.liferay.poshi.runner.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Leslie Wong
 */
public class JSONUtil {

	public static Object get(JSONObject jsonObject, String name)
		throws Exception {

		return jsonObject.get(name);
	}

	public static boolean getBoolean(JSONObject jsonObject, String name)
		throws Exception {

		return jsonObject.getBoolean(name);
	}

	public static double getDouble(JSONObject jsonObject, String name)
		throws Exception {

		return jsonObject.getDouble(name);
	}

	public static int getInt(JSONObject jsonObject, String name)
		throws Exception {

		return jsonObject.getInt(name);
	}

	public static JSONArray getJSONArray(JSONObject jsonObject, String name)
		throws Exception {

		return jsonObject.getJSONArray(name);
	}

	public static JSONObject getJSONObject(JSONObject jsonObject, String name)
		throws Exception {

		return jsonObject.getJSONObject(name);
	}

	public static long getLong(JSONObject jsonObject, String name)
		throws Exception {

		return jsonObject.getLong(name);
	}

	public static String getString(JSONObject jsonObject, String name)
		throws Exception {

		return jsonObject.getString(name);
	}

	public static JSONArray toJSONArray(String content) throws Exception {
		return new JSONArray(content);
	}

	public static JSONObject toJSONObject(String content) throws Exception {
		return new JSONObject(content);
	}

}