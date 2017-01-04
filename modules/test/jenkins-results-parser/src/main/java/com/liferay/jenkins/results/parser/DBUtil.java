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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Leslie Wong
 */
public class DBUtil {

	public static List<Map<String, Object>> executeQuery(String query)
		throws Exception {

		List<Map<String, Object>> queryResult = new ArrayList<>();

		Properties properties = JenkinsResultsParserUtil.getBuildProperties();

		Connection conn = null;
		Statement stmt = null;

		try {
			Class.forName("org.mariadb.jdbc.Driver");

			conn = DriverManager.getConnection(
				properties.getProperty("db.url"),
				properties.getProperty("testray.db.password"),
				properties.getProperty("testray.db.user.name"));

			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(query);

			ResultSetMetaData rsmd = rs.getMetaData();

			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();

				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					row.put(rsmd.getColumnName(i), (String)rs.getObject(i));
				}

				queryResult.add(row);
			}

			rs.close();
			stmt.close();
			conn.close();
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			}
			catch (SQLException sqle) {
			}

			try {
				if (conn != null) {
					conn.close();
				}
			}
			catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		}

		return queryResult;
	}

}