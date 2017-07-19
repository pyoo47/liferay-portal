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

package com.liferay.jenkins.results.parser.exception;

import org.dom4j.Element;

/**
 * @author Kevin Yen
 */
public class UnknownElementException extends RuntimeException {

	public UnknownElementException(Element element) {
		super(_generateMessage(element));
	}

	public UnknownElementException(Element element, Throwable cause) {
		super(_generateMessage(element), cause);
	}

	private static String _generateMessage(Element element) {
		return "Unknown element '" + element.getName() + "'";
	}

}