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

import com.liferay.poshi.runner.util.Dom4JUtil;

import java.io.BufferedReader;
import java.io.StringReader;

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

	protected String getReadableCommandTitle() {
		return "test" + attributeValue("name");
	}

}