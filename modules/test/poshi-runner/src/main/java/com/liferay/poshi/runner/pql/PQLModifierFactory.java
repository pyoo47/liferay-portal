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

/**
 * @author Michael Hashimoto
 */
public class PQLModifierFactory {

	public static PQLModifier newInstance(String modifier) throws Exception {
		PQLModifier.validateModifier(modifier);

		if (modifier.equals("NOT")) {
			return new PQLModifier(modifier) {

				public Object modify(Object objectValue) throws Exception {
					String modifier = getModifier();

					if (objectValue == null) {
						throw new Exception(
							"Invalid usage of '" + modifier + "' modifier.");
					}

					if (!(objectValue instanceof Boolean)) {
						throw new Exception(
							"Invalid usage of '" + modifier + "' modifier.");
					}

					Boolean booleanValue = (Boolean)objectValue;

					return !booleanValue;
				}

			};
		}

		throw new Exception("Unsupported '" + modifier + "' modifier.");
	}

}