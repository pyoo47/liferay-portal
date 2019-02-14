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

package com.liferay.jenkins.results.parser.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Kevin Yen
 */
public class PropertyGetter {

	public static String getProperty(
		Properties properties, String name, String... opts) {

		Map<String, List<PropertyParameter>> propertySelectionMap =
			new HashMap<>();

		properties = _getPropertiesByBaseName(properties, name);

		for (String propertyName : properties.stringPropertyNames()) {
			List<PropertyParameter> propertyParameterList =
				PropertyParameterFactory.getPropertySelectionList(propertyName);

			propertySelectionMap.put(propertyName, propertyParameterList);
		}

		_removeEntriesByVariantLength(propertySelectionMap, opts);

		Set<Map.Entry<String, List<PropertyParameter>>> entrySet =
			propertySelectionMap.entrySet();

		Iterator<Map.Entry<String, List<PropertyParameter>>> iterator =
			entrySet.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, List<PropertyParameter>> entry = iterator.next();

			if (!_propertyVariantsMatchesStrings(
					entry.getValue(), Arrays.asList(opts))) {

				iterator.remove();
			}
		}

		List<Map.Entry<String, List<PropertyParameter>>> entryList =
			new ArrayList<>(propertySelectionMap.entrySet());

		Collections.sort(entryList, new PropertySelectionEntrySetComparator());

		if (entryList.isEmpty()) {
			return null;
		}

		String mostMatchesPropertyName = entryList.get(0).getKey();

		return properties.getProperty(mostMatchesPropertyName);
	}

	public static List<String> getValues(
		Properties properties, String name, PropertyParameter... opts) {

		Map<String, List<String>> propertiesMap = new HashMap<>();

		properties = _getPropertiesByBaseName(properties, name);

		for (String propertyName : properties.stringPropertyNames()) {
			propertiesMap.put(
				propertyName,
				PropertyUtil.getPropertyNameParameters(propertyName));
		}

		_removeEntriesWithDifferentVariantSize(propertiesMap, opts);

		Set<Map.Entry<String, List<String>>> entrySet =
			propertiesMap.entrySet();

		Iterator<Map.Entry<String, List<String>>> iterator =
			entrySet.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, List<String>> entry = iterator.next();

			if (!_propertyVariantsMatchesStrings(
					Arrays.asList(opts), entry.getValue())) {

				iterator.remove();
			}
		}

		List<String> values = new ArrayList<>();

		for (Map.Entry<String, List<String>> entry : entrySet) {
			values.add(properties.getProperty(entry.getKey()));
		}

		return values;
	}

	private static Properties _getPropertiesByBaseName(
		Properties properties, String baseName) {

		Properties filteredProperties = new Properties();

		for (String propertyName : properties.stringPropertyNames()) {
			if (propertyName.equals(baseName) ||
				propertyName.startsWith(baseName + "[")) {

				filteredProperties.put(
					propertyName, properties.getProperty(propertyName));
			}
		}

		return filteredProperties;
	}

	private static boolean _propertyVariantsMatchesStrings(
		List<PropertyParameter> propertyParameters, List<String> strings) {

		for (int i = 0; i < propertyParameters.size(); i++) {
			PropertyParameter propertyParameter = propertyParameters.get(i);

			if (!propertyParameter.matches(strings.get(i))) {
				return false;
			}
		}

		return true;
	}

	private static <U, V> void _removeEntriesByVariantLength(
		Map<String, List<U>> map, V[] opts) {

		Set<Map.Entry<String, List<U>>> entrySet = map.entrySet();

		Iterator<Map.Entry<String, List<U>>> iterator = entrySet.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, List<U>> entry = iterator.next();

			List<U> propertyVariants = entry.getValue();

			if (propertyVariants.size() > opts.length) {
				iterator.remove();
			}
		}
	}

	private static <U, V> void _removeEntriesWithDifferentVariantSize(
		Map<String, List<U>> map, V[] opts) {

		Set<Map.Entry<String, List<U>>> entrySet = map.entrySet();

		Iterator<Map.Entry<String, List<U>>> iterator = entrySet.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, List<U>> entry = iterator.next();

			List<U> propertyVariants = entry.getValue();

			if (propertyVariants.size() != opts.length) {
				iterator.remove();
			}
		}
	}

	private static class PropertySelectionEntrySetComparator
		implements Comparator<Map.Entry<String, List<PropertyParameter>>> {

		@Override
		public int compare(
			Map.Entry<String, List<PropertyParameter>> entry1,
			Map.Entry<String, List<PropertyParameter>> entry2) {

			List<PropertyParameter> propertySelections1 = entry1.getValue();
			List<PropertyParameter> propertySelections2 = entry2.getValue();

			int compareSize =
				propertySelections2.size() - propertySelections1.size();

			if (compareSize != 0) {
				return compareSize;
			}

			for (int i = 0; i < propertySelections1.size(); i++) {
				PropertyParameter selection1 = propertySelections1.get(i);
				PropertyParameter selection2 = propertySelections2.get(i);

				Class<?> selection1Class = selection1.getClass();

				if (!selection1Class.equals(selection2.getClass())) {
					if (PropertyParameter.class.equals(selection2.getClass())) {
						return 1;
					}
					else if (PropertyParameter.class.equals(
								selection1.getClass())) {

						return -1;
					}
				}
			}

			return 0;
		}

	}

}