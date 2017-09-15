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

import java.io.File;
import java.io.IOException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * @author Kevin Yen
 */
public class PrerequisiteRulesParser {

	public static Set<PrerequisiteRule> parse(File rulesFile)
		throws DocumentException, IOException {

		Document document = Dom4JUtil.parse(rulesFile);

		Element prerequisitesElement = document.getRootElement();

		List<Element> ruleElements = prerequisitesElement.elements("rule");

		Set<PrerequisiteRule> prerequisiteRules = new HashSet<>();

		for (Element ruleElement : ruleElements) {
			String description = "";

			if (ruleElement.attributeValue("description") != null) {
				description = ruleElement.attributeValue("description");
			}

			Element assignElement = getRequiredChildElement(
				"assign", ruleElement);

			BuildMatcher assignMatcher = parseJobElement(
				getRequiredChildElement("job", assignElement));

			Element prerequisiteElement = getRequiredChildElement(
				"prerequisite", ruleElement);

			BuildMatcher prerequisiteMatcher = parseJobElement(
				getRequiredChildElement("job", prerequisiteElement));

			Element invokeElement = getRequiredChildElement(
				"invoke", ruleElement);

			BuildMatcher invokeMatcher = parseJobElement(
				getRequiredChildElement("job", invokeElement));

			Element discardElement = ruleElement.element("discard");

			BuildMatcher discardBuildMatcher = null;

			if (discardElement != null) {
				discardBuildMatcher = parseJobElement(
					getRequiredChildElement("job", discardElement));
			}

			prerequisiteRules.add(
				new PrerequisiteRule(
					assignMatcher, description, discardBuildMatcher,
					invokeMatcher, prerequisiteMatcher));
		}

		return prerequisiteRules;
	}

	public static BuildMatcher parseJobElement(Element jobElement) {
		BuildMatcher buildMatcher = new BuildMatcher();

		String attributeValue = jobElement.attributeValue("type");

		if (attributeValue != null) {
			if (attributeValue.equals("AxisBuild")) {
				AxisBuildMatcher axisBuildMatcher = new AxisBuildMatcher();

				Element axisElement = jobElement.element("axis");

				Pattern axisNamePattern = parseTextElement(axisElement);

				axisBuildMatcher.setAxisNumberPattern(axisNamePattern);

				buildMatcher = axisBuildMatcher;
			}
			else if (attributeValue.equals("BatchBuild")) {
				buildMatcher.setBuildType(BatchBuild.class);
			}
		}

		for (Element element : jobElement.elements()) {
			String elementName = element.getName();

			if (elementName.equals("name")) {
				Pattern namePattern = parseTextElement(element);

				buildMatcher.setJobNamePattern(namePattern);
			}
			else if (elementName.equals("status")) {
				Pattern statusPattern = parseTextElement(element);

				buildMatcher.setStatusPattern(statusPattern);
			}
			else if (elementName.equals("result")) {
				Pattern resultPattern = parseTextElement(element);

				buildMatcher.setResultPattern(resultPattern);
			}
			else if (elementName.equals("parameter")) {
				Pattern parameterNamePattern = parseTextElement(
					getRequiredChildElement("name", element));

				Pattern parameterValuePattern = parseTextElement(
					getRequiredChildElement("value", element));

				buildMatcher.addParameterPatterns(
					parameterNamePattern, parameterValuePattern);
			}
			else if (elementName.equals("has-downstream-jobs") &&
					 element.isTextOnly()) {

				buildMatcher.setCheckHasDownstreamBuilds(true);
			}
		}

		return buildMatcher;
	}

	public static Pattern parseTextElement(Element element) {
		if (element.isTextOnly()) {
			String text = element.getText();

			if (text.isEmpty()) {
				throw new RuntimeException(
					JenkinsResultsParserUtil.combine(
						"Text field of element ", element.getName(),
						" is empty"));
			}

			StringBuilder sb = new StringBuilder();

			sb.append("^");

			for (String parsedText : text.split("\\s*,\\s*")) {
				if (sb.length() > 1) {
					sb.append("|");
				}

				sb.append(Pattern.quote(parsedText));
			}

			sb.append("$");

			return Pattern.compile(sb.toString());
		}

		StringBuilder sb = new StringBuilder();

		for (Element childElement : element.elements()) {
			String name = childElement.getName();

			if (name.equals("contains")) {
				String text = childElement.getText();

				if (text.isEmpty()) {
					throw new RuntimeException(
						JenkinsResultsParserUtil.combine(
							"Text field of element ", childElement.getName(),
							" is empty"));
				}

				if (sb.length() > 0) {
					sb.append("|");
				}

				sb.append(Pattern.quote(text));
			}
			else if (name.equals("regex")) {
				String text = childElement.getText();

				if (text.isEmpty()) {
					throw new RuntimeException(
						JenkinsResultsParserUtil.combine(
							"Text field of element ", childElement.getName(),
							" is empty"));
				}

				if (sb.length() > 0) {
					sb.append("|");
				}

				sb.append("(");
				sb.append(text);
				sb.append(")");
			}
			else {
				throw new RuntimeException(
					"Unknown element " + element.getName());
			}
		}

		return Pattern.compile(sb.toString());
	}

	protected static Element getRequiredChildElement(
		String requiredChildElementName, Element parentElement) {

		Element childElement = parentElement.element(requiredChildElementName);

		if (childElement == null) {
			throw new RuntimeException(
				JenkinsResultsParserUtil.combine(
					parentElement.getName(),
					" is missing required child element ",
					requiredChildElementName));
		}

		return childElement;
	}

}