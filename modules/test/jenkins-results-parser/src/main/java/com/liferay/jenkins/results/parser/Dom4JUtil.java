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
	public static Element toCodeSnippetElement(String content) {
		Element codeElement = new DefaultElement("code");
		Element preElement = new DefaultElement("pre");
		
		preElement.add(codeElement);
	
		codeElement.addText(content);
	
		return preElement;
	}

	public static Element toStrongElement(Object content) {
		Element strongElement = new DefaultElement("strong");
	
		if (content instanceof Element) {
			strongElement.add((Element)content);
	
			return strongElement;
		}
	
		if (content instanceof String) {
			strongElement.addText(content.toString());
	
			return strongElement;
		}
	
		throw new IllegalArgumentException("content must be Element or String");
	}

}
