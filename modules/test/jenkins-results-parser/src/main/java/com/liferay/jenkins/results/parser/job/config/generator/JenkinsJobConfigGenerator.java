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

package com.liferay.jenkins.results.parser.job.config.generator;

import com.liferay.jenkins.results.parser.Dom4JUtil;
import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;

/**
 * @author Cesar Polanco
 */
public class JenkinsJobConfigGenerator {

	public Map getJobConfig(Map<String, String> properties) throws Exception {
		Map<String, String> generatedPropertiesMap = new HashMap<>(properties);
		Map<String, String> environmentSlavesMap = _getEnvironmentSlavesMap(
			properties);
		Set<String> globalProperties = new TreeSet<>();
		Map<String, Map<String, String>> jobNamesToJobProperties =
			new TreeMap<>();
		Map<String, Map<String, String>> jobNamesToMasterJobProperties =
			new TreeMap<>();
		Map<String, String> linuxEnvironmentVariablesMap =
			_getLinuxEnvironmentVariablesMap();
		Set<String> masterHostnames = new TreeSet<>();
		Map<String, String> masterHostnamesToJobNames = new TreeMap<>();
		Map<String, Map<String, String>> masterHostnamesToMasterProperties =
			new TreeMap<>();
		Map<String, String> osxEnvironmentVariablesMap =
			_getOSXEnvironmentVariablesMap();
		Set<String> slaveHostnames = new TreeSet<>();
		Map<String, String> windowsEnvironmentVariablesMap =
			_getWindowsEnvironmentVariablesMap();

		Set<Map.Entry<String, String>> propertiesSet = properties.entrySet();

		Iterator<Map.Entry<String, String>> propertiesIterator =
			propertiesSet.iterator();

		while (propertiesIterator.hasNext()) {
			Map.Entry<String, String> entry = propertiesIterator.next();

			String key = entry.getKey();
			String value = entry.getValue();

			if (key.startsWith("global.property(")) {
				String propertyName = _getPropertyName(key);

				globalProperties.add(propertyName);

				if (generatedPropertiesMap.containsKey(
						"global.property.names")) {

					propertyName = generatedPropertiesMap.get(
						"global.property.names") + propertyName + ",";
				}

				generatedPropertiesMap.put(
					"global.property.names", propertyName);
			}
			else if (key.startsWith("master.job.names(")) {
				String masterHostname = _getMasterHostName(key);

				masterHostnames.add(masterHostname);
				masterHostnamesToJobNames.put(masterHostname, value);

				Map<String, String> childJobNamesMap = new HashMap<>();
				List<String> parentJobNames = new ArrayList<>();
				Set<String> topLevelJobNames = new TreeSet<>();

				String currentParentJobName1 = null;
				String currentParentJobName2 = null;

				for (String jobName : value.split(",")) {
					int x = jobName.indexOf("(");

					if (x != -1) {
						String jobBranchName = _getJobBranchName(jobName);

						generatedPropertiesMap.put(
							"job.portal.branch.name(" + jobName + ")",
							jobBranchName);

						String jobVariationName = _getJobVariationName(jobName);

						generatedPropertiesMap.put(
							"job.variation.name(" + jobName + ")",
							jobVariationName);
					}
					else {
						generatedPropertiesMap.put(
							"job.variation.name(" + jobName + ")", "");
					}

					String jobShortName = _getJobShortName(jobName);

					generatedPropertiesMap.put(
						"job.short.name(" + jobName + ")", jobShortName);

					String jobContent = _readFileToString(
						new File(
							"template/jobs/" + jobShortName + "/config.xml"));

					String blockableBuildTriggerConfig =
						"hudson.plugins.parameterizedtrigger." +
							"BlockableBuildTriggerConfig";

					if (jobContent.contains("Build Flow") ||
						jobContent.contains(
							"com.cloudbees.plugins.flow.BuildFlow")) {

						generatedPropertiesMap.put(
							"job.content.type(" + jobName + ")", "BuildFlow");

						childJobNamesMap.put(jobName, jobName);
						parentJobNames.add(jobName);
					}
					else if ((jobContent.contains(
								blockableBuildTriggerConfig) ||
							  jobContent.contains(
								  blockableBuildTriggerConfig)) &&
							 (jobName.contains("fixpack") ||
							  jobName.contains("maintenance-tags"))) {

						generatedPropertiesMap.put(
							"job.content.type(" + jobName + ")",
							"BuildTrigger");

						parentJobNames.add(jobName);

						Set<String> triggerBuilderChildJobNames =
							new TreeSet<>();

						String patternToFind = JenkinsResultsParserUtil.combine(
							"hudson\\.plugins\\", ".parameterizedtrigger\\",
							".(TriggerBuilder|BuildTrigger)(.*?)/hudson",
							"\\.plugins\\.parameterizedtrigger\\",
							".(TriggerBuilder|BuildTrigger)");

						triggerBuilderChildJobNames.add(jobName);

						Pattern triggerBuilderPattern = Pattern.compile(
							patternToFind, Pattern.DOTALL);

						Matcher triggerBuilderMatcher =
							triggerBuilderPattern.matcher(jobContent);

						while (triggerBuilderMatcher.find()) {
							String triggerBuilder = triggerBuilderMatcher.group(
								2);

							String triggerBuilderChildJobName =
								_getTriggerBuilderChildJobName(triggerBuilder);

							if (!triggerBuilderChildJobName.equals(
									"@!child.job.names!@")) {

								triggerBuilderChildJobName =
									triggerBuilderChildJobName.replace(
										"@!job.variation.name!@",
										generatedPropertiesMap.get(
											"job.variation.name(" + jobName +
												")"));

								triggerBuilderChildJobNames.add(
									triggerBuilderChildJobName);
							}
						}

						childJobNamesMap.put(
							jobName,
							_joinStrings(triggerBuilderChildJobNames, ","));
					}

					if (jobContent.contains("Top Level")) {
						topLevelJobNames.add(jobName);
					}

					if (parentJobNames.contains(jobName) &&
						topLevelJobNames.contains(jobName)) {

						currentParentJobName1 = jobName;
					}
					else if (parentJobNames.contains(jobName)) {
						currentParentJobName2 = jobName;
					}

					String currentParentJobName = null;

					if (currentParentJobName2 != null) {
						String currentParentJobShortName2 =
							generatedPropertiesMap.get(
								"job.short.name(" + currentParentJobName2 +
									")");
						String currentParentJobVariationName2 =
							generatedPropertiesMap.get(
								"job.variation.name(" + currentParentJobName2 +
									")");
						boolean memberOfCurrentParentJob = false;

						if (jobName.startsWith(currentParentJobShortName2)) {
							if (jobName.endsWith(
									"(" + currentParentJobVariationName2 +
										")") ||
								currentParentJobVariationName2.equals("")) {

								memberOfCurrentParentJob = true;
							}
						}

						if (memberOfCurrentParentJob &&
							!jobName.equals(currentParentJobName2)) {

							currentParentJobName = currentParentJobName2;
						}
						else if (!memberOfCurrentParentJob) {
							currentParentJobName2 = null;
						}
					}

					if ((currentParentJobName == null) &&
						(currentParentJobName1 != null)) {

						String currentParentJobShortName1 =
							generatedPropertiesMap.get(
								"job.short.name(" + currentParentJobName1 +
									")");
						String currentParentJobVariationName1 =
							generatedPropertiesMap.get(
								"job.variation.name(" + currentParentJobName1 +
									")");
						boolean memberOfCurrentParentJob = false;

						if (jobName.startsWith(currentParentJobShortName1)) {
							if (jobName.endsWith(
									"(" + currentParentJobVariationName1 +
										")") ||
								currentParentJobVariationName1.equals("")) {

								memberOfCurrentParentJob = true;
							}
						}

						if (memberOfCurrentParentJob &&
							!jobName.equals(currentParentJobName1)) {

							currentParentJobName = currentParentJobName1;
						}
						else if (!memberOfCurrentParentJob) {
							currentParentJobName1 = null;
						}
					}

					if (currentParentJobName != null) {
						Set<String> childJobNames = new TreeSet<>();

						String childJobNamesString = childJobNamesMap.get(
							currentParentJobName);

						for (String childJobName :
								childJobNamesString.split(",")) {

							childJobNames.add(childJobName);
						}

						if (jobName.contains("[component.name]")) {
							String componentNames = generatedPropertiesMap.get(
								"job.component.names(" + jobName + ")");

							if (componentNames == null) {
								componentNames = "portal-acceptance";
							}

							for (String componentName :
									componentNames.split(",")) {

								childJobNames.add(
									jobName.replace(
										"[component.name]",
										"[" + componentName + "]"));
							}
						}
						else {
							childJobNames.add(jobName);
						}

						childJobNamesMap.put(
							currentParentJobName,
							_joinStrings(childJobNames, ","));
					}
				}

				for (String parentJobName : parentJobNames) {
					String childJobNames = childJobNamesMap.get(parentJobName);

					if (!childJobNames.contains(",")) {
						System.out.println(
							"ERROR: Parent job is missing children jobs: " +
								parentJobName + "\n");

						throw new Exception();
					}

					generatedPropertiesMap.put(
						"child.job.names(" + parentJobName + ")",
						childJobNamesMap.get(parentJobName));
				}

				StringBuilder sb = new StringBuilder();

				Element topLevelGroupElement = Dom4JUtil.getNewElement(
					"top-level-include-jobs");

				for (String topLevelJobName : topLevelJobNames) {
					String excludeTopLevelView = generatedPropertiesMap.get(
						"job.property(" + topLevelJobName +
							"/exclude.top.level.view)");

					if ((excludeTopLevelView == null) ||
						!excludeTopLevelView.equals("true")) {

						Element topLevelJobNameElement =
							Dom4JUtil.getNewElement(
								"string", topLevelGroupElement);

						topLevelJobNameElement.setText(topLevelJobName);
					}
				}

				generatedPropertiesMap.put(
					"master.top.level.jobs.xml(" + masterHostname + ")",
					_getFormattedXML(topLevelGroupElement));

				String masterPrimaryView = generatedPropertiesMap.get(
					"master.primary.view(" + masterHostname + ")");

				if (masterPrimaryView == null) {
					if (topLevelJobNames.isEmpty()) {
						generatedPropertiesMap.put(
							"master.primary.view(" + masterHostname + ")",
							"All");
					}
					else {
						generatedPropertiesMap.put(
							"master.primary.view(" + masterHostname + ")",
							"Top Level");
					}
				}

				Element topLevelElement = Dom4JUtil.getNewElement("jobs");

				for (String parentJobName : parentJobNames) {
					String childJobNames = generatedPropertiesMap.get(
						"child.job.names(" + parentJobName + ")");

					String[] childJobNamesArray = childJobNames.split(",");

					Arrays.sort(childJobNamesArray);

					topLevelElement.add(
						_generateListViewByParent(
							parentJobName, childJobNamesArray));
				}

				generatedPropertiesMap.put(
					"master.list.views.xml(" + masterHostname + ")",
					_getFormattedXML(topLevelElement));

				for (String parentJobName : parentJobNames) {
					sb = new StringBuilder();

					String childJobNames = generatedPropertiesMap.get(
						"child.job.names(" + parentJobName + ")");
					List<String> childJobNamesList = new ArrayList<>();

					for (String childJobName : childJobNames.split(",")) {
						if (childJobName.equals(parentJobName)) {
							continue;
						}

						if (childJobName.contains("-githubpost-start") ||
							childJobName.contains("-githubpost-stop") ||
							childJobName.contains("-patcherpost-start") ||
							childJobName.contains("-patcherpost-stop") ||
							childJobName.contains("-prepare") ||
							childJobName.contains("-source") ||
							childJobName.contains("-test-results")) {

							continue;
						}

						String jobContentType = generatedPropertiesMap.get(
							"job.content.type(" + parentJobName + ")");

						if (jobContentType.equals("BuildTrigger")) {
							String shortParentJobName =
								generatedPropertiesMap.get(
									"job.short.name(" + parentJobName + ")");

							String shortChildJobName = _getShortChildJobName(
								childJobName);

							if (!shortChildJobName.contains(
									shortParentJobName)) {

								continue;
							}
						}

						boolean ignoreChildJobName = false;

						String jobName = childJobName;

						if (jobName.matches(".*[^\\[]+\\[[a-z\\-]+\\].*")) {
							jobName = jobName.replaceAll(
								"\\[[a-z\\-]+\\]", "[component.name]");
						}

						String componentNamesIgnore =
							generatedPropertiesMap.get(
								"job.component.names.ignore(" + jobName + ")");

						if (componentNamesIgnore != null) {
							for (String componentNameIgnore :
									componentNamesIgnore.split(",")) {

								if (childJobName.contains(
										"[" + componentNameIgnore + "]")) {

									ignoreChildJobName = true;

									break;
								}
							}
						}

						sb.append("\n");
						sb.append("\t{\n");

						if (ignoreChildJobName) {
							sb.append("\t\tignore(ABORTED) {\n\t");
						}

						sb.append("\t\tJOB_INVOCATION_");
						sb.append(childJobNamesList.size());
						sb.append(" = build(\"");
						sb.append(childJobName);

						String customJobParameters = generatedPropertiesMap.get(
							"job.property(" + jobName +
								"/custom.job.parameters)");

						if (customJobParameters != null) {
							sb.append("\", @!custom.job.parameters!@)\n");

							generatedPropertiesMap.put(
								"custom.job.parameters(" + parentJobName + ")",
								customJobParameters);
						}
						else {
							sb.append("\", @!job.parameters!@)\n");
						}

						if (ignoreChildJobName) {
							sb.append("\t\t}\n");
						}

						sb.append("\t},");

						childJobNamesList.add(childJobName);
					}

					if (sb.length() != 0) {
						sb.deleteCharAt(sb.length() - 1);
					}

					generatedPropertiesMap.put(
						"build.flow.downstream.jobs.xml(" + parentJobName + ")",
						sb.toString());
					generatedPropertiesMap.put(
						"child.job.names(" + parentJobName + ")",
						_joinStrings(childJobNamesList, ","));

					sb = new StringBuilder();

					for (int i = 0; i < childJobNamesList.size(); i++) {
						sb.append("JOB_INVOCATION_");
						sb.append(i);
						sb.append(" = build\n");
					}

					String jobInvocationList =
						"job.invocation.instantiate.list";

					generatedPropertiesMap.put(
						JenkinsResultsParserUtil.combine(
							jobInvocationList, parentJobName, ")"),
						sb.toString());

					sb = new StringBuilder();

					for (int i = 0; i < childJobNamesList.size(); i++) {
						sb.append("JOB_INVOCATIONS += JOB_INVOCATION_");
						sb.append(i);
						sb.append("\n");
					}

					generatedPropertiesMap.put(
						"job.invocation.make.list(" + parentJobName + ")",
						sb.toString());
				}
			}
			else if (key.startsWith("master.job.property(")) {
				String jobName = _getJobNameFromProperty(key);

				String masterHostname = _getMasterHostNameFromProperty(key);
				String propertyName = _getPropertyNameForJobProperty(key);

				Map<String, String> masterJobProperties =
					jobNamesToMasterJobProperties.get(jobName);

				if (masterJobProperties == null) {
					masterJobProperties = new TreeMap<>();

					jobNamesToMasterJobProperties.put(
						jobName, masterJobProperties);
				}

				masterJobProperties.put(propertyName, value);

				generatedPropertiesMap.put(
					"master.job.properties(" + masterHostname + "/" + jobName +
						")",
					_joinStrings(masterJobProperties.keySet(), ","));
			}
			else if (key.startsWith("master.property(")) {
				String masterHostname = _getMasterHostNameFromProperty(key);
				String propertyName = _getPropertyNameForJobProperty(key);

				Map<String, String> masterProperties =
					masterHostnamesToMasterProperties.get(masterHostname);

				if (masterProperties == null) {
					masterProperties = new TreeMap<>();

					masterHostnamesToMasterProperties.put(
						masterHostname, masterProperties);
				}

				masterProperties.put(propertyName, value);

				generatedPropertiesMap.put(
					"master.properties(" + masterHostname + ")",
					_joinStrings(masterProperties.keySet(), ","));
			}
			else if (key.startsWith("master.slaves(")) {
				String masterHostname = _getMasterHostName(key);

				masterHostnames.add(masterHostname);

				if (value.contains("..")) {
					StringBuilder sb = new StringBuilder();

					value = JenkinsResultsParserUtil.expandSlaveRange(value);

					for (String slaveHostname : value.split(",")) {
						sb.append(slaveHostname);
						sb.append("(");

						String environmentSlaveOSType = "";

						if (environmentSlavesMap.containsKey(slaveHostname)) {
							String environmentSlaveKey =
								environmentSlavesMap.get(slaveHostname);

							environmentSlaveOSType = _getEnvironmentSlaveOSType(
								environmentSlaveKey);
						}

						if (environmentSlaveOSType.contains("osx")) {
							environmentSlaveOSType = "osx";
						}
						else if (environmentSlaveOSType.contains("solaris")) {
							environmentSlaveOSType = "solaris";
						}
						else if (environmentSlaveOSType.contains("win")) {
							environmentSlaveOSType = "windows";
						}
						else {
							environmentSlaveOSType = "linux";
						}

						sb.append(environmentSlaveOSType);

						sb.append("),");
					}

					value = sb.toString();
				}

				generatedPropertiesMap.put(
					"master.slaves.txt(" + masterHostname + ")",
					value.replaceAll(",", "\n").trim());

				// StringBuilder sb = new StringBuilder();

		//

				// sb.append("<slaves>\n");

				Element slaveBaseElement = Dom4JUtil.getNewElement(
					"slaves-base");
				Element slaves = Dom4JUtil.getNewElement(
					"slaves", slaveBaseElement);

				for (String slaveHostname : value.split(",")) {
					if (slaveHostname.contains("(")) {
						slaveHostname = _getSlaveHostname(slaveHostname);
					}

					Element slaveConfigXML = _getSlaveConfigXMLContent(
						slaveHostname, generatedPropertiesMap);

					generatedPropertiesMap.put(
						slaveHostname + ".config.xml.content",
						_getFormattedXML(slaveConfigXML));

					// sb.append(slaveConfigXMLContent);

		  //

					// sb.append("\n");

					Dom4JUtil.addToElement(slaves, slaveConfigXML);

					slaveHostnames.add(slaveHostname);

					generatedPropertiesMap.put(
						"slave.master(" + slaveHostname + ")", masterHostname);
				}

				// sb.append("\t</slaves>");

				generatedPropertiesMap.put(
					"master.slaves.xml(" + masterHostname + ")",
					_getFormattedXML(slaveBaseElement));

				// sb = new StringBuilder();

				Element slaveHostNameBaseElement = Dom4JUtil.getNewElement(
					"slave_host_name_base");

				for (String slaveHostname : value.split(",")) {
					if (slaveHostname.contains("(")) {
						slaveHostname = _getSlaveHostname(slaveHostname);
					}

					Element slaveHostnameElement = Dom4JUtil.getNewElement(
						"string", slaveHostNameBaseElement);

					slaveHostnameElement.setText(slaveHostname);

					// sb.append("<string>");
					// sb.append(slaveHostname);
					// sb.append("</string>");

				}

				generatedPropertiesMap.put(
					"master.axes.label.slaves.xml(" + masterHostname + ")",
					_getFormattedXML(slaveHostNameBaseElement));
			}
			else if (key.startsWith("job.property(")) {
				String jobName = _getMasterHostNameFromProperty(key);
				String propertyName = _getPropertyNameForJobProperty(key);

				Map<String, String> jobProperties = jobNamesToJobProperties.get(
					jobName);

				if (jobProperties == null) {
					jobProperties = new TreeMap<>();

					jobNamesToJobProperties.put(jobName, jobProperties);
				}

				jobProperties.put(propertyName, value);

				generatedPropertiesMap.put(
					"job.properties(" + jobName + ")",
					_joinStrings(jobProperties.keySet(), ","));
			}
		}

		generatedPropertiesMap.put(
			"master.hostnames", _joinStrings(masterHostnames, ","));

		generatedPropertiesMap.put(
			"slave.hostnames", _joinStrings(slaveHostnames, ","));

		int i = 0;

		for (String masterHostname : masterHostnames) {
			if (masterHostname.startsWith("test-")) {
				generatedPropertiesMap.put(
					"trigger.day." + masterHostname, Integer.toString(i % 7));

				generatedPropertiesMap.put(
					"trigger.hour." + masterHostname, Integer.toString(i % 24));

				i++;
			}
		}

		return generatedPropertiesMap;
	}

	private Element _generateListViewByParent(
		String parentJobName, String[] childJobNames) {

		Element listViewElement = Dom4JUtil.getNewElement("listView");

		Element ownerElement = Dom4JUtil.getNewElement(
			"owner", listViewElement);

		ownerElement.addAttribute("class", "hudson");
		ownerElement.addAttribute("reference", "../../..");

		Element nameElement = Dom4JUtil.getNewElement("name", listViewElement);
		Element filterExecutorElement = Dom4JUtil.getNewElement(
			"filterExecutors", listViewElement);
		Element filterQueueElement = Dom4JUtil.getNewElement(
			"filterQueue", listViewElement);
		Element propertiesElement = Dom4JUtil.getNewElement(
			"properties", listViewElement);
		Element jobNamesElement = Dom4JUtil.getNewElement(
			"jobNames", listViewElement);
		Element jobFiltersElement = Dom4JUtil.getNewElement(
			"jobFilters", listViewElement);
		Element columnsElement = Dom4JUtil.getNewElement(
			"columns", listViewElement);
		Element recurseElement = Dom4JUtil.getNewElement(
			"recurse", listViewElement);

		propertiesElement.addAttribute(
			"class", "hudson.model.ViewsPropertyList");

		Element comparatorElement = Dom4JUtil.getNewElement(
			"comparator", jobNamesElement,
			"class=\"hudson.util.CaseInsensitiveComparator\"");

		for (String childJob : childJobNames) {
			Element nameStringElement = Dom4JUtil.getNewElement(
				"string", jobNamesElement);

			nameStringElement.setText(childJob);
		}

		Element statusColumnElement = Dom4JUtil.getNewElement(
			"hudson.views.statusColumn", columnsElement);
		Element weatherColumnElement = Dom4JUtil.getNewElement(
			"hudson.views.WeatherColumn", columnsElement);
		Element jobColumnElement = Dom4JUtil.getNewElement(
			"hudson.viewss.JobColumn", columnsElement);
		Element descriptionColumnElement = Dom4JUtil.getNewElement(
			"jenkins.plugins.extracolumns.DescriptionColumn", columnsElement);
		Element lastSuccessElement = Dom4JUtil.getNewElement(
			"hudson.views.lastSuccessColumn", columnsElement);
		Element lastFailureElement = Dom4JUtil.getNewElement(
			"hudson.views.lastFailureColumn", columnsElement);
		Element lastDurationElement = Dom4JUtil.getNewElement(
			"hudson.views.lastDurationColumn", columnsElement);
		Element buildButtonElement = Dom4JUtil.getNewElement(
			"hudson.views.BuildButtonColumn", columnsElement);

		descriptionColumnElement.addAttribute("plugin", "extra-columns@1.11");

		Element displayNameElement = Dom4JUtil.getNewElement(
			"displayName", descriptionColumnElement);
		Element trimElement = Dom4JUtil.getNewElement(
			"trim", descriptionColumnElement);
		Element displayLengthElement = Dom4JUtil.getNewElement(
			"displayLength", descriptionColumnElement);

		nameElement.setText(parentJobName);
		filterExecutorElement.setText("true");
		filterQueueElement.setText("true");
		displayNameElement.setText("false");
		trimElement.setText("false");
		displayLengthElement.setText("1");
		recurseElement.setText("false");

		return listViewElement;
	}

	private String _getEnvironmentSlaveOSType(String property) {
		Pattern envSlaveOSPattern = Pattern.compile("^(\\w+)\\.");

		Matcher envSlaveOSMatcher = envSlaveOSPattern.matcher(property);

		return envSlaveOSMatcher.group(1);
	}

	private Map<String, String> _getEnvironmentSlavesMap(
		Map<String, String> properties) {

		Map<String, String> environmentSlavesMap = new TreeMap<>();

		Set<Map.Entry<String, String>> propertiesSet = properties.entrySet();

		Iterator<Map.Entry<String, String>> propertiesIterator =
			propertiesSet.iterator();

		while (propertiesIterator.hasNext()) {
			Map.Entry<String, String> entry = propertiesIterator.next();

			String key = entry.getKey();

			if (!key.startsWith("environment.slaves(")) {
				continue;
			}

			String propertyName = _getPropertyNameIncludingSlaves(key);

			String value = entry.getValue();

			if (value.contains("..")) {
				value = JenkinsResultsParserUtil.expandSlaveRange(value);
			}

			for (String slaveHostname : value.split(",")) {
				environmentSlavesMap.put(slaveHostname, propertyName);
			}
		}

		return environmentSlavesMap;
	}

	private String _getFormattedXML(Element parent) {
		StringBuilder sb = new StringBuilder();

		List<Element> childElements = parent.elements();

		for (Element el : childElements) {
			try {
				sb.append(Dom4JUtil.format(el));

				sb.append("\n");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				//throw ioe;
			}
		}

		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}

		return sb.toString();
	}

	private String _getJobBranchName(String property) {
		Pattern jobBranchNamePattern = Pattern.compile("\\((\\w+?)[_|\\)]");

		Matcher jobBranchNameMatcher = jobBranchNamePattern.matcher(property);

		return jobBranchNameMatcher.group(1);
	}

	private String _getJobNameFromProperty(String property) {
		Pattern jobNamePattern = Pattern.compile("/(\\w+)/");

		Matcher jobNameMatcher = jobNamePattern.matcher(property);

		return jobNameMatcher.group(1);
	}

	private String _getJobShortName(String property) {
		Pattern jobShortNamePattern = Pattern.compile("(\\w+)\\(?");

		Matcher jobShortNameMatcher = jobShortNamePattern.matcher(property);

		return jobShortNameMatcher.group(1);
	}

	private String _getJobVariationName(String property) {
		Pattern jobVariationNamePattern = Pattern.compile("\\((\\w+)\\)");

		Matcher jobVariationNameMatcher = jobVariationNamePattern.matcher(
			property);

		return jobVariationNameMatcher.group(1);
	}

	private Map<String, String> _getLinuxEnvironmentVariablesMap() {
		Map<String, String> linuxEnvironmentVariablesMap = new TreeMap<>();

		String pathValue = JenkinsResultsParserUtil.combine(
			"/bin:", "/opt/android/sdk:/opt/android/sdk/platform-tools:",
			"/opt/android/sdk/tools:/opt/ibm/db2/V10.5/bin:/opt/java/ant/bin:",
			"/opt/java/jdk/bin:/opt/java/maven/bin:/opt/oracle/bin:",
			"/opt/sybase/OCS-16_0/bin:/sbin:/usr/bin:/usr/local/bin:",
			"/usr/local/sbin:/usr/sbin");

		linuxEnvironmentVariablesMap.put("PATH", pathValue);

		return linuxEnvironmentVariablesMap;
	}

	private String _getMasterHostName(String property) {
		Pattern masterHostNamePattern = Pattern.compile("\\((\\w+)\\)");

		Matcher masterHostNameMatcher = masterHostNamePattern.matcher(property);

		return masterHostNameMatcher.group(1);
	}

	private String _getMasterHostNameFromProperty(String property) {
		Pattern masterHostNamePattern = Pattern.compile("\\((\\w+)/");

		Matcher masterHostNameMatcher = masterHostNamePattern.matcher(property);

		return masterHostNameMatcher.group(1);
	}

	private Map<String, String> _getOSXEnvironmentVariablesMap() {
		Map<String, String> osxEnvironmentVariablesMap = new TreeMap<>();

		String pathValue = JenkinsResultsParserUtil.combine(
			"/bin:/opt/java/ant/bin:/opt/java/jdk/bin:/opt/java/maven/bin:",
			"/sbin:/usr/bin:/usr/local/bin:/usr/local/sbin:/usr/sbin");

		osxEnvironmentVariablesMap.put("HOME", "/Users/administrator");
		osxEnvironmentVariablesMap.put("PATH", pathValue);

		return osxEnvironmentVariablesMap;
	}

	private String _getPropertyName(String property) {
		Pattern propertyNamePattern = Pattern.compile("\\((.+)\\)");

		Matcher propertyNameMatcher = propertyNamePattern.matcher(property);

		return propertyNameMatcher.group(1);
	}

	private String _getPropertyNameForJobProperty(String property) {
		Pattern propertyNamePattern = Pattern.compile("/.+?/(.+)\\)");

		Matcher propertyNameMatcher = propertyNamePattern.matcher(property);

		return propertyNameMatcher.group(1);
	}

	private String _getPropertyNameIncludingSlaves(String property) {
		Pattern propNamePattern = Pattern.compile("slaves\\((.+)\\)");

		Matcher propNameMatcher = propNamePattern.matcher(property);

		return propNameMatcher.group(1);
	}

	private String _getShortChildJobName(String property) {
		Pattern shortChildJobNamePattern = Pattern.compile("(\\w+)[\\[|\\(]?");

		Matcher shortChildJobNameMatcher = shortChildJobNamePattern.matcher(
			property);

		return shortChildJobNameMatcher.group(1);
	}

	private Element _getSlaveConfigXMLContent(
		String slaveHostname, Map<String, String> properties) {

		String slaveLabel = slaveHostname;

		Map<String, String> environmentSlavesMap = _getEnvironmentSlavesMap(
			properties);
		Map<String, String> linuxEnvironmentVariablesMap =
			_getLinuxEnvironmentVariablesMap();
		Map<String, String> osxEnvironmentVariablesMap =
			_getOSXEnvironmentVariablesMap();
		Map<String, String> windowsEnvironmentVariablesMap =
			_getWindowsEnvironmentVariablesMap();

		if (environmentSlavesMap.containsKey(slaveHostname)) {
			slaveLabel = environmentSlavesMap.get(slaveHostname);
		}

		Element slaveElement = Dom4JUtil.getNewElement("slave");

		Element slaveNameElement = Dom4JUtil.getNewElement(
			"name", slaveElement);
		Element slaveDescriptionElement = Dom4JUtil.getNewElement(
			"description", slaveElement);
		Element slaveRemoteFSElement = Dom4JUtil.getNewElement(
			"remoteFS", slaveElement);
		Element slaveExecutorsElement = Dom4JUtil.getNewElement(
			"numExecutors", slaveElement);
		Element slaveModeElement = Dom4JUtil.getNewElement(
			"mode", slaveElement);
		Element slaveRetentionElement = Dom4JUtil.getNewElement(
			"retentionStrategy", slaveElement);
		Element slaveLauncherElement = Dom4JUtil.getNewElement(
			"launcher", slaveElement);
		Element slaveLabelElement = Dom4JUtil.getNewElement(
			"label", slaveElement);
		Element nodePropertiesElement = Dom4JUtil.getNewElement(
			"nodeProperties", slaveElement);

		Element environmentVariableElement = Dom4JUtil.getNewElement(
			"hudson.slaves.EnvironmentVariablesNodeProperty",
			nodePropertiesElement);

		Element envSerializationElement = Dom4JUtil.getNewElement(
			"envVars", environmentVariableElement);

		Element parentsElement = Dom4JUtil.getNewElement(
			"unserializable-parents", envSerializationElement);
		Element treeMapElement = Dom4JUtil.getNewElement(
			"tree-map", envSerializationElement);

		Element treeMapDefaultElement = Dom4JUtil.getNewElement(
			"default", treeMapElement);

		slaveRetentionElement.addAttribute(
			"class", "hudson.slaves.RetentionStrategy$Always");
		slaveLauncherElement.addAttribute(
			"class", "hudson.slaves.JNLPLauncher");
		envSerializationElement.addAttribute("serialization", "custom");

		slaveNameElement.setText(slaveHostname);
		slaveRemoteFSElement.setText("/opt/java/jenkins");
		slaveExecutorsElement.setText("1");
		slaveModeElement.setText("NORMAL");
		slaveLabelElement.setText(slaveLabel);

		if (slaveLabel.contains("osx")) {
			List<Element> osxEnvironmentVariablesElements =
				_getSlaveEnvironmentVariables(
					osxEnvironmentVariablesMap, slaveHostname);

			Dom4JUtil.addToElement(
				treeMapElement, osxEnvironmentVariablesElements);
		}
		else if (slaveLabel.contains("win")) {
			List<Element> windowsEnvironmentVariablesElements =
				_getSlaveEnvironmentVariables(
					windowsEnvironmentVariablesMap, slaveHostname);

			Dom4JUtil.addToElement(
				treeMapElement, windowsEnvironmentVariablesElements);
		}
		else {
			List<Element> linuxEnvironmentVariablesElements =
				_getSlaveEnvironmentVariables(
					linuxEnvironmentVariablesMap, slaveHostname);

			Dom4JUtil.addToElement(
				treeMapElement, linuxEnvironmentVariablesElements);
		}

		return slaveElement;
	}

	private List<Element> _getSlaveEnvironmentVariables(
		Map<String, String> environmentVariablesMap, String hostname) {

		environmentVariablesMap.put("HOSTNAME", hostname + ".lax.liferay.com");

		List<Element> environmentVariableElements = new ArrayList<>();

		Element sizeElement = Dom4JUtil.getNewElement("int");

		sizeElement.setText(Integer.toString(environmentVariablesMap.size()));

		environmentVariableElements.add(sizeElement);

		Set<String> environmentVariables = environmentVariablesMap.keySet();

		for (String environmentVariable : environmentVariables) {
			Element nameElement = Dom4JUtil.getNewElement("string");
			Element valueElement = Dom4JUtil.getNewElement("string");

			nameElement.setText(environmentVariable);
			valueElement.setText(
				environmentVariablesMap.get(environmentVariable));

			environmentVariableElements.add(nameElement);
			environmentVariableElements.add(valueElement);
		}

		return environmentVariableElements;
	}

	private String _getSlaveHostname(String property) {
		Pattern slaveHostnamePattern = Pattern.compile("^(\\w+)\\(");

		Matcher slaveHostnameMatcher = slaveHostnamePattern.matcher(property);

		return slaveHostnameMatcher.group(1);
	}

	private String _getTriggerBuilderChildJobName(String property) {
		Pattern triggerBuilderChildJobNamePattern = Pattern.compile(
			"<projects>(.+)</projects>", Pattern.DOTALL);

		Matcher triggerBuilderChildJobNameMatcher =
			triggerBuilderChildJobNamePattern.matcher(property);

		return triggerBuilderChildJobNameMatcher.group(1);
	}

	private Map<String, String> _getWindowsEnvironmentVariablesMap() {
		Map<String, String> windowsEnvironmentVariablesMap = new TreeMap<>();

		String pathValue = JenkinsResultsParserUtil.combine(
			"/c/ant/bin:/c/Perl64/bin:/c/Program Files/7-Zip:",
			"/c/Program Files/IBM/SQLLIB/BIN:",
			"/c/Program Files/Java/jdk1.7.0_55/bin:",
			"/c/Program Files/MySQL/MySQL Server 5.6/bin:",
			"/c/Program Files/Microsoft SQL Server/100/Tools/Binn:",
			"/c/Program Files/Microsoft SQL Server/110/Tools/Binn:",
			"/c/Program Files/Microsoft SQL Server/120/Tools/Binn:",
			"/c/Program Files/Microsoft SQL Server/130/Tools/Binn:",
			"/c/Program Files/Microsoft SQL Server/Client ",
			"SDK/ODBC/110/Tools/Binn:",
			"/c/Program Files/Microsoft SQL Server/Client ",
			"SDK/ODBC/130/Tools/Binn:/c/Windows:/c/Windows/system32");

		windowsEnvironmentVariablesMap.put("HOME", "/c/Users/Administrator");
		windowsEnvironmentVariablesMap.put(
			"JAVA_HOME", "/c/Program Files/Java/jdk1.7.0_55");
		windowsEnvironmentVariablesMap.put("PATH", pathValue);

		return windowsEnvironmentVariablesMap;
	}

	private String _joinStrings(List<String> inputList, String separator) {
		String conjoinedString = "";

		for (String inputString : inputList) {
			conjoinedString = conjoinedString + inputString + separator;
		}

		if (conjoinedString.length() > 0) {
			conjoinedString = conjoinedString.substring(
				0, conjoinedString.length() - 1);
		}

		return conjoinedString;
	}

	private String _joinStrings(Set<String> inputSet, String separator) {
		String conjoinedString = "";
		Iterator<String> it = inputSet.iterator();

		while (it.hasNext()) {
			conjoinedString = conjoinedString + it.next() + separator;
		}

		conjoinedString = conjoinedString.substring(
			0, conjoinedString.length() - 1);

		return conjoinedString;
	}

	private String _readFileToString(File fileToRead) {
		String result = "";

		try {
			List<String> lines = Files.readAllLines(fileToRead.toPath());

			for (String line : lines) {
				result = JenkinsResultsParserUtil.combine(result, line, "\n");
			}

			result = result.substring(0, result.length() - 1);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return result;
	}

}