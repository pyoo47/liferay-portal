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

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * @author Peter Yoo
 */
public class Dom4JUtil {

	public static void addToElement(Element element, Object... items) {
		for (int i = 0; i < items.length; i++) {
			Object item = items[i];

			if (item == null) {
				continue;
			}

			if (item instanceof Element) {
				element.add((Element)item);

				continue;
			}

			if (item instanceof String) {
				element.addText((String)item);

				continue;
			}

			throw new IllegalArgumentException(
				"Only Elements and Strings may be added.");
		}
	}

	public static Element getNewAnchorElement(
		String href, Element parentElement, String text) {

		Element anchorElement = null;

		if (parentElement == null) {
			anchorElement = new DefaultElement("a");
		}
		else {
			anchorElement = getNewElement("a", parentElement);
		}

		anchorElement.addAttribute("href", href);

		anchorElement.addText(text);

		return anchorElement;
	}

	public static Element getNewAnchorElement(String href, String text) {
		return getNewAnchorElement(href, null, text);
	}

	public static Element getNewElement(
		String childElementTag, Element parentElement, Object... items) {

		Element childElement = new DefaultElement(childElementTag);

		parentElement.add(childElement);

		if ((items != null) && (items.length > 0)) {
			addToElement(childElement, items);
		}

		return childElement;
	}

	public static Element toCodeSnippetElement(String content) {
		return wrapWithNewElement(wrapWithNewElement(content, "code"), "pre");
	}

	public static Element wrapWithNewElement(
		Element element, String wrapperTag) {

		Element wrapperElement = new DefaultElement(wrapperTag);

		wrapperElement.add(element);

		return wrapperElement;
	}

	public static Element wrapWithNewElement(
		String content, String wrapperTag) {

		Element wrapperElement = new DefaultElement(wrapperTag);

		wrapperElement.addText(content);

		return wrapperElement;
	}

}