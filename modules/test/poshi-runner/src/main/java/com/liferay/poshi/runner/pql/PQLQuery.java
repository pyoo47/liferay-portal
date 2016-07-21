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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Hashimoto
 */
public class PQLQuery implements PQLQueryEntity {

	public PQLQuery(String query, Properties properties) throws Exception {
		_properties = properties;
		_query = query;

		_findAll();
	}

	public void _findAll() throws Exception {
		String query = _query;

		for (PQLFactory pqlFactory : _factories) {
			System.out.println(pqlFactory.getStart(query));
		}

		Pattern conditionalPattern = PQLConditionalFactory.getPattern();
		Pattern keywordPattern = PQLKeywordFactory.getPattern();

		Matcher conditionalMatcher = conditionalPattern.matcher(query);
		Matcher keywordMatcher = keywordPattern.matcher(query);
		Matcher subqueryMatcher = _subqueryPattern.matcher(query);

		while (true) {
			PQLEntity pqlEntity = null;

			int x = -1;

			if (conditionalMatcher.find()) {
				x = conditionalMatcher.start();

				pqlEntity = PQLEntity.CONDITONAL;
			}

			if (keywordMatcher.find()) {
				int y = keywordMatcher.start();

				if (y < x) {
					x = y;

					pqlEntity = PQLEntity.KEYWORD;
				}
			}

			if (subqueryMatcher.find()) {
				int y = subqueryMatcher.start();

				if (y < x) {
					x = y;

					pqlEntity = PQLEntity.SUBQUERY;
				}
			}

			if (pqlEntity == null) {
				break;
			}

			switch (pqlEntity) {
				case CONDITONAL:
					PQLConditional pqlConditional = new PQLConditional(
						conditionalMatcher.group(), _properties);

					PQLQueryEntity pqlQueryEntity = pqlConditional;

					_pqlEntities.add(pqlConditional);

					query = query.substring(conditionalMatcher.end());
				break;

				case KEYWORD:
					PQLKeyword pqlKeyword = PQLKeywordFactory.build(
						keywordMatcher.group(1));

					_pqlEntities.add(pqlKeyword);

					query = query.substring(keywordMatcher.end());
				break;

				case SUBQUERY:
					String subquery = subqueryMatcher.group();

					subquery = subquery.substring(1, subquery.length() - 1);

					PQLQuery pqlSubquery = new PQLQuery(subquery, _properties);

					_pqlEntities.add(pqlSubquery);

					query = query.substring(subqueryMatcher.end());
				break;

				default:
					throw new Exception("Invalid PQL Entity!");
			}

			conditionalMatcher = conditionalPattern.matcher(query);
			keywordMatcher = keywordPattern.matcher(query);
			subqueryMatcher = _subqueryPattern.matcher(query);
		}
	}

	public boolean getResult() throws Exception {
		boolean result = false;

		PQLKeyword pqlKeywordFinal = null;

		for (Object pqlEntity : _pqlEntities) {
			if (pqlEntity instanceof PQLQuery) {
				PQLQuery pqlQuery = (PQLQuery)pqlEntity;

				if (pqlKeywordFinal == null) {
					result = pqlQuery.getResult();
				}
				else {
					result = pqlKeywordFinal.addResult(
						result, pqlQuery.getResult());
				}
			}
			else if (pqlEntity instanceof PQLConditional) {
				PQLConditional pqlConditional = (PQLConditional)pqlEntity;

				if (pqlKeywordFinal == null) {
					result = pqlConditional.getResult();
				}
				else {
					result = pqlKeywordFinal.addResult(
						result, pqlConditional.getResult());
				}
			}
			else if (pqlEntity instanceof PQLKeyword) {
				PQLKeyword pqlKeyword = (PQLKeyword)pqlEntity;

				if (pqlKeywordFinal == null) {
					pqlKeywordFinal = pqlKeyword;
				}

				if (!pqlKeywordFinal.equals(pqlKeyword)) {
					throw new Exception("Invalid syntax!");
				}
			}
			else {
				throw new Exception("Bad entity!");
			}
		}

		return result;
	}

	private static List<PQLFactory> _factories = new ArrayList<PQLFactory>();
	private static final Pattern _subqueryPattern = Pattern.compile(
		"\\((.*?)\\)+");

	private final List _pqlEntities = new ArrayList();
	private final Properties _properties;
	private final String _query;

	private enum PQLEntity {
		CONDITONAL, KEYWORD, SUBQUERY
	}

	static {
		_factories.add(PQLQueryFactory.getInstance());
	}

}