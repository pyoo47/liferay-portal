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

package com.liferay.poshi.runner.elements;

import static com.liferay.poshi.runner.elements.ReadableSyntaxKeys.BACKGROUND;
import static com.liferay.poshi.runner.elements.ReadableSyntaxKeys.DESCRIPTION;
import static com.liferay.poshi.runner.elements.ReadableSyntaxKeys.FEATURE;
import static com.liferay.poshi.runner.elements.ReadableSyntaxKeys.PRIORITY;
import static com.liferay.poshi.runner.elements.ReadableSyntaxKeys.SCENARIO;
import static com.liferay.poshi.runner.elements.ReadableSyntaxKeys.SET_UP;
import static com.liferay.poshi.runner.elements.ReadableSyntaxKeys.TEAR_DOWN;
import static com.liferay.poshi.runner.elements.ReadableSyntaxKeys.THESE_PROPERTIES;
import static com.liferay.poshi.runner.elements.ReadableSyntaxKeys.THESE_VARIABLES;
import static com.liferay.poshi.runner.util.StringPool.COLON;
import static com.liferay.poshi.runner.util.StringPool.SPACE;

import com.liferay.poshi.runner.util.Dom4JUtil;
import com.liferay.poshi.runner.util.StringUtil;

import java.io.BufferedReader;
import java.io.StringReader;

import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * @author Kenji Heigel
 */
public class CommandElement extends PoshiElement {

	public CommandElement(Element element) {
		this("command", element);
	}

	public CommandElement(String readableSyntax) {
		this("command", readableSyntax);
	}

	public CommandElement(String name, Element element) {
		super(name, element);
	}

	public CommandElement(String name, String readableSyntax) {
		super(name, readableSyntax);
	}

	@Override
	public void parseReadableSyntax(String readableSyntax) {
		try (BufferedReader bufferedReader = new BufferedReader(
				new StringReader(readableSyntax))) {

			StringBuilder sb = new StringBuilder();

			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				String endKey = " {";

				if (line.endsWith(endKey)) {
					String startKey = "test";

					if (line.startsWith(startKey)) {
						int start = startKey.length();
						int end = line.length() - endKey.length();

						String name = line.substring(start, end);

						addAttribute("name", name);
					}

					continue;
				}

				if (line.equals(");")) {
					sb.append(line);

					addElementFromReadableSyntax(sb.toString());

					sb.setLength(0);

					continue;
				}

				if (line.endsWith(";")) {
					addElementFromReadableSyntax(line);

					continue;
				}

				if (line.equals("}")) {
					continue;
				}

				if (line.startsWith("@")) {
					String name = getNameFromAssignment(line);
					String value = getValueFromAssignment(line);

					addAttribute(name, value);

					continue;
				}

				sb.append(line);
			}
		}
		catch (Exception e) {
			System.out.println("Unable to generate the 'command' element");
		}
	}

	@Override
	public String toOldReadableSyntax() {
		StringBuilder sb = new StringBuilder();

		sb.append("\n");
		sb.append(getOldReadableCommandTitle());

		if (attributeValue("name") != null) {
			String name = attributeValue("name");

			sb.append(toPhrase(name));
		}

		if (attributeValue("description") != null) {
			String description = attributeValue("description");

			sb.append("\n");
			sb.append(DESCRIPTION);
			sb.append(": ");
			sb.append(description);
		}

		if (attributeValue("priority") != null) {
			String priority = attributeValue("priority");

			sb.append("\n");
			sb.append(PRIORITY);
			sb.append(": ");
			sb.append(priority);
		}

		sb.append(super.toOldReadableSyntax());

		return sb.toString();
	}

	@Override
	public String toReadableSyntax() {
		StringBuilder sb = new StringBuilder();

		for (Attribute attribute : Dom4JUtil.toAttributeList(attributeList())) {
			String name = attribute.getName();

			if (name.equals("name")) {
				continue;
			}

			sb.append("\n\t@");

			String value = attribute.getValue();

			sb.append(getAssignment(name, value));
		}

		String readableCommandTitle = getReadableCommandTitle();
		String readableSyntax = super.toReadableSyntax();

		sb.append(createReadableBlock(readableCommandTitle, readableSyntax));

		return sb.toString();
	}

	protected String getOldReadableCommandTitle() {
		return SCENARIO + COLON + SPACE;
	}

	protected String getReadableCommandTitle() {
		return "test" + attributeValue("name");
	}

	private void _addDescriptionAttribute(String readableSyntax) {
		String description = getAttributeValue(
			DESCRIPTION + COLON, readableSyntax);

		addAttribute("description", description);
	}

	private void _addPriorityAttribute(String readableSyntax) {
		String priority = getAttributeValue(PRIORITY + COLON, readableSyntax);

		addAttribute("priority", priority);
	}

	private String _getCommandName(String readableSyntax) {
		String scenario = getAttributeValue(SCENARIO + COLON, readableSyntax);

		return StringUtil.removeSpaces(scenario);
	}

}