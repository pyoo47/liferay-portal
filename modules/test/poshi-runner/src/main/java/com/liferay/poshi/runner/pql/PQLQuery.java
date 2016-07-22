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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Michael Hashimoto
 */
public class PQLQuery {

	public PQLQuery(String query, Properties properties) throws Exception {
		_query = query;
		_properties = properties;

		_processQuery(_query);
	}

	public boolean getResult() throws Exception {
		return _result;
	}

	private void _processQuery(String query) throws Exception {
		query = query.trim();

		while (true) {
			boolean queryEntityFound = false;
			PQLQueryEntityFactory targetPQLQueryEntityFactory = null;

			for (PQLQueryEntityFactory pqlQueryEntityFactory :
					_pqlQueryEntityFactories) {

				if (pqlQueryEntityFactory.getStart(query) == 0) {
					queryEntityFound = true;

					targetPQLQueryEntityFactory = pqlQueryEntityFactory;

					break;
				}
			}

			if (targetPQLQueryEntityFactory != null) {
				PQLQueryEntity pqlQueryEntity =
					targetPQLQueryEntityFactory.build(query, _properties);

				_processResult(pqlQueryEntity);

				query = targetPQLQueryEntityFactory.removeFromQuery(query);

				query = query.trim();

				if (query.equals("")) {
					break;
				}
			}
			else if (!queryEntityFound) {
				throw new Exception("Invalid query!");
			}
		}
	}

	private void _processResult(PQLQueryEntity pqlQueryEntity)
		throws Exception {

		if (pqlQueryEntity instanceof PQLKeywordConditional) {
			if (_result == null) {
				throw new Exception(
					"Do not start query with conditional keyword!");
			}

			if ((_pqlKeywordConditional != null) &&
				!_pqlKeywordConditional.equals(pqlQueryEntity)) {

				throw new Exception("Do not change the conditional keyword!");
			}

			if (_pqlKeywordNot != null) {
				throw new Exception(
					"'NOT' can not come before a conditional keyword!");
			}

			_pqlKeywordConditional = (PQLKeywordConditional)pqlQueryEntity;
		}
		else if (pqlQueryEntity instanceof PQLKeywordNot) {
			_pqlKeywordNot = (PQLKeywordNot)pqlQueryEntity;
		}
		else if (pqlQueryEntity instanceof PQLQueryEntityResult) {
			PQLQueryEntityResult pqlQueryEntityResult =
				(PQLQueryEntityResult)pqlQueryEntity;

			Boolean result = pqlQueryEntityResult.getResult();

			if (_pqlKeywordConditional != null) {
				result = _pqlKeywordConditional.applyConditionalKeyword(
					_result, result);
			}

			if (_pqlKeywordNot != null) {
				result = _pqlKeywordNot.applyKeyword(result);

				_pqlKeywordNot = null;
			}

			_result = result;
		}
	}

	private static final List<PQLQueryEntityFactory> _pqlQueryEntityFactories =
		new ArrayList<>();

	static {
		_pqlQueryEntityFactories.add(PQLConditionalFactory.getInstance());
		_pqlQueryEntityFactories.add(PQLKeywordFactory.getInstance());
		_pqlQueryEntityFactories.add(PQLSubqueryFactory.getInstance());
	}

	private PQLKeywordConditional _pqlKeywordConditional;
	private PQLKeywordNot _pqlKeywordNot;
	private final Properties _properties;
	private final String _query;
	private Boolean _result;

}