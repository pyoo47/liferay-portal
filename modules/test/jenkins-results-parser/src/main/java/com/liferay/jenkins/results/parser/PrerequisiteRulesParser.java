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

import com.liferay.jenkins.results.parser.build.criteria.AxisBuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.AxisNumberBuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.BatchBuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.BuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.HasDownstreamBuildsBuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.NameBuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.NameContainBuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.NameDoesNotContainBuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.ParameterBuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.ParameterContainBuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.ParameterDoesNotContainBuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.ResultBuildCriteria;
import com.liferay.jenkins.results.parser.build.criteria.StatusBuildCriteria;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * @author Kevin Yen
 */
public class PrerequisiteRulesParser {

	public static PrerequisiteRules parse(File rulesFile)
		throws DocumentException, IOException {

		Document document = Dom4JUtil.parse(rulesFile);

		Element prerequisitesElement = document.getRootElement();

		List<Element> ruleElements = prerequisitesElement.elements("rule");

		PrerequisiteRules prerequisiteRules = new PrerequisiteRules();

		for (Element ruleElement : ruleElements) {
			String description = "";

			if (ruleElement.attributeValue("description") != null) {
				description = ruleElement.attributeValue("description");
			}

			Element triggerElement = ruleElement.element("trigger");

			Element jobElement = triggerElement.element("job");

			List<BuildCriteria> applicableBuildCriterias = new ArrayList<>();

			applicableBuildCriterias.addAll(
				parseJobRequirementBuildCriterias(jobElement));

			Element prerequisiteElement = ruleElement.element("prerequisite");

			jobElement = prerequisiteElement.element("job");

			List<BuildCriteria> completeBuildCriterias = new ArrayList<>();
			List<BuildCriteria> passingBuildCriterias = new ArrayList<>();
			List<BuildCriteria> prerequisiteBuildCriterias = new ArrayList<>();

			prerequisiteBuildCriterias.addAll(
				parseJobRequirementBuildCriterias(jobElement));

			String type = jobElement.attributeValue("type");

			if (type != null) {
				if (type.equals("AxisBuild")) {
					prerequisiteBuildCriterias.addAll(
						parseAxisBuildCriterias(jobElement));
				}
				else if (type.equals("BatchBuild")) {
					prerequisiteBuildCriterias.add(new BatchBuildCriteria());
				}
			}

			if (jobElement.element("status") != null) {
				Element statusElement = jobElement.element("status");

				String text = statusElement.getText();

				if (text.isEmpty()) {
					throw new PrerequisiteRulesException(
						"The value field of " + statusElement.getName() +
							" cannot be empty");
				}

				completeBuildCriterias.add(new StatusBuildCriteria(text));
			}

			if (jobElement.element("has-downstream-jobs") != null) {
				completeBuildCriterias.add(
					new HasDownstreamBuildsBuildCriteria());
			}

			if (jobElement.element("result") != null) {
				Element resultElement = jobElement.element("result");

				String text = resultElement.getText();

				if (text.isEmpty()) {
					throw new PrerequisiteRulesException(
						"The value field of " + resultElement.getName() +
							" cannot be empty");
				}

				passingBuildCriterias.add(new ResultBuildCriteria(text));
			}

			prerequisiteRules.add(
				new PrerequisiteRule(
					description, applicableBuildCriterias,
					prerequisiteBuildCriterias, completeBuildCriterias,
					passingBuildCriterias));
		}

		return prerequisiteRules;
	}

	protected static List<BuildCriteria> parseAxisBuildCriterias(
		Element jobElement) {

		List<BuildCriteria> buildCriterias = new ArrayList<>();

		String type = jobElement.attributeValue("type");

		if (!type.equals("AxisBuild")) {
			return buildCriterias;
		}

		buildCriterias.add(new AxisBuildCriteria());

		Element axisElement = jobElement.element("axis");

		if (axisElement != null) {
			String text = axisElement.getText();

			if (text.isEmpty()) {
				throw new PrerequisiteRulesException(
					"The value field of " + axisElement.getName() +
						" cannot be empty");
			}

			buildCriterias.add(new AxisNumberBuildCriteria(text));
		}

		return buildCriterias;
	}

	protected static List<BuildCriteria> parseJobRequirementBuildCriterias(
		Element jobElement) {

		List<BuildCriteria> buildCriterias = new ArrayList<>();

		Element nameElement = jobElement.element("name");

		if (nameElement != null) {
			buildCriterias.addAll(parseNameBuildCriterias(nameElement));
		}

		Element parameterElement = jobElement.element("parameter");

		if (parameterElement != null) {
			buildCriterias.addAll(
				parseParameterBuildCriteria(parameterElement));
		}

		return buildCriterias;
	}

	protected static List<BuildCriteria> parseNameBuildCriterias(
		Element nameElement) {

		List<BuildCriteria> nameBuildCriterias = new ArrayList<>();

		if (nameElement.element("contain") != null) {
			Element containElement = nameElement.element("contain");

			String text = containElement.getText();

			if (text.isEmpty()) {
				throw new PrerequisiteRulesException(
					"The value field of " + containElement.getName() +
						" cannot be empty");
			}

			nameBuildCriterias.add(new NameContainBuildCriteria(text));
		}

		if (nameElement.element("does-not-contain") != null) {
			Element doesNotContainElement = nameElement.element(
				"does-not-contain");

			String text = doesNotContainElement.getText();

			if (text.isEmpty()) {
				throw new PrerequisiteRulesException(
					"The value field of " + doesNotContainElement.getName() +
						" cannot be empty");
			}

			nameBuildCriterias.add(new NameDoesNotContainBuildCriteria(text));
		}

		if ((nameElement.element("contain") == null) &&
			(nameElement.element("does-not-contain") == null)) {

			String text = nameElement.getText();

			if (text.isEmpty()) {
				throw new PrerequisiteRulesException(
					"The value field of " + nameElement.getName() +
						" cannot be empty");
			}

			nameBuildCriterias.add(new NameBuildCriteria(text));
		}

		return nameBuildCriterias;
	}

	protected static List<BuildCriteria> parseParameterBuildCriteria(
		Element parameterElement) {

		List<BuildCriteria> parameterBuildCriterias = new ArrayList<>();

		if (parameterElement.element("contain") != null) {
			String name = parameterElement.attributeValue("name");

			if ((name == null) || name.isEmpty()) {
				throw new PrerequisiteRulesException(
					"The attribute field of " + parameterElement.getName() +
						" cannot be empty");
			}

			Element containElement = parameterElement.element("contain");

			String value = containElement.getText();

			if (value.isEmpty()) {
				throw new PrerequisiteRulesException(
					"The value field of " + containElement.getName() +
						" cannot be empty");
			}

			parameterBuildCriterias.add(
				new ParameterContainBuildCriteria(name, value));
		}

		if (parameterElement.element("does-not-contain") != null) {
			String name = parameterElement.attributeValue("name");

			if ((name == null) || name.isEmpty()) {
				throw new PrerequisiteRulesException(
					"The attribute field of " + parameterElement.getName() +
						" cannot be empty");
			}

			Element doesNotContainElement = parameterElement.element(
				"does-not-contain");

			String value = doesNotContainElement.getText();

			if (value.isEmpty()) {
				throw new PrerequisiteRulesException(
					"The value field of " + doesNotContainElement.getName() +
						" cannot be empty");
			}

			parameterBuildCriterias.add(
				new ParameterDoesNotContainBuildCriteria(name, value));
		}

		if ((parameterElement.element("contain") == null) &&
			(parameterElement.element("does-not-contain") == null)) {

			String name = parameterElement.attributeValue("name");

			if ((name == null) || name.isEmpty()) {
				throw new PrerequisiteRulesException(
					"The attribute field of " + parameterElement.getName() +
						" cannot be empty");
			}

			String value = parameterElement.getText();

			if (value.isEmpty()) {
				throw new PrerequisiteRulesException(
					"The value field of " + parameterElement.getName() +
						" cannot be empty");
			}

			parameterBuildCriterias.add(
				new ParameterBuildCriteria(name, value));
		}

		return parameterBuildCriterias;
	}

}