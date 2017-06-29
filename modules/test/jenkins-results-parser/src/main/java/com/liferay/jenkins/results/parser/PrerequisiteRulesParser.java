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

import com.liferay.jenkins.results.parser.exception.EmptyElementTextException;
import com.liferay.jenkins.results.parser.exception.MissingElementException;
import com.liferay.jenkins.results.parser.exception.UnknownElementException;
import com.liferay.jenkins.results.parser.matcher.AxisBuildMatcher;
import com.liferay.jenkins.results.parser.matcher.BuildMatcher;

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

			Element assignElement = ruleElement.element("assign");

			if (assignElement == null) {
				throw new MissingElementException("assign");
			}

			Element jobElement = assignElement.element("job");

			BuildMatcher assignMatcher = parseJobElement(jobElement);

			Element prerequisiteElement = ruleElement.element("prerequisite");

			if (prerequisiteElement == null) {
				throw new MissingElementException("prerequisite");
			}

			jobElement = prerequisiteElement.element("job");

			BuildMatcher prerequisiteMatcher = parseJobElement(jobElement);

			Element invokeElement = ruleElement.element("invoke");

			if (invokeElement == null) {
				throw new MissingElementException("invoke");
			}

			jobElement = invokeElement.element("job");

			BuildMatcher invokeMatcher = parseJobElement(jobElement);

			Element discardElement = ruleElement.element("discard");

			BuildMatcher discardBuildMatcher = null;

			if (discardElement != null) {
				jobElement = discardElement.element("job");

				discardBuildMatcher = parseJobElement(jobElement);
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
				Element parameterNameElement = element.element("name");

				if (parameterNameElement == null) {
					throw new MissingElementException("name");
				}

				Pattern parameterNamePattern = parseTextElement(
					parameterNameElement);

				Element parameterValueElement = element.element("value");

				if (parameterValueElement == null) {
					throw new MissingElementException("value");
				}

				Pattern parameterValuePattern = parseTextElement(
					parameterValueElement);

				buildMatcher.addParameterNameValuePatterns(
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
				throw new EmptyElementTextException(element);
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
					throw new EmptyElementTextException(childElement);
				}

				if (sb.length() > 0) {
					sb.append("|");
				}

				sb.append(Pattern.quote(text));
			}
			else if (name.equals("regex")) {
				String text = childElement.getText();

				if (text.isEmpty()) {
					throw new EmptyElementTextException(childElement);
				}

				if (sb.length() > 0) {
					sb.append("|");
				}

				sb.append("(");
				sb.append(text);
				sb.append(")");
			}
			else {
				throw new UnknownElementException(childElement);
			}
		}

		return Pattern.compile(sb.toString());
	}

}
