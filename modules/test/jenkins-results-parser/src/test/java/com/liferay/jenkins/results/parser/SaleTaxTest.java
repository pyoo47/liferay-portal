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

import com.liferay.jenkins.results.parser.java.task.Item;
import com.liferay.jenkins.results.parser.java.task.ItemParser;
import com.liferay.jenkins.results.parser.java.task.Receipt;
import com.liferay.jenkins.results.parser.java.task.ShoppingCart;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.net.URI;
import java.net.URL;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Brittney Nguyen
 */
public class SaleTaxTest {

	@Before
	public void setUp() {
		cart = new ShoppingCart();
	}

	@Test
	public void testCompareOutputs() throws Exception {
		String[] outputs = {"output1.txt", "output2.txt", "output3.txt"};

		String[] expectedOutputs = {
			"dependencies/expected_output1.txt",
			"dependencies/expected_output2.txt",
			"dependencies/expected_output3.txt"
		};
		int count = 0;
		Class<?> clazz = Receipt.class;

		for (String file : expectedOutputs) {
			URL resourceURL = clazz.getResource(file);

			URI resourceURI = resourceURL.toURI();

			Path resourcePath = Paths.get(resourceURI);

			File expectedFile = resourcePath.toFile();

			File output = new File(outputs[count]);

			boolean areFilesEqual = FileUtils.contentEquals(
				expectedFile, output);

			System.out.println(areFilesEqual);

			count += 1;
		}
	}

	@Test
	public void testGenerateOutputs() throws Exception {
		String[] inputs = {
			"dependencies/input1.txt", "dependencies/input2.txt",
			"dependencies/input3.txt"
		};

		int count = 1;
		Class<?> clazz = Receipt.class;

		for (String file : inputs) {
			URL resourceURL = clazz.getResource(file);

			URI resourceURI = resourceURL.toURI();

			Path resourcePath = Paths.get(resourceURI);

			File resourceFile = resourcePath.toFile();

			String path = resourceFile.getPath();

			ItemParser parser = new ItemParser(path);

			cart = parser.getCart();

			ArrayList<Item> items = cart.getItems();

			for (Item item : items) {
				item.setTax();
			}

			Receipt receipt = new Receipt();

			String printedReceipt = receipt.printReceipt(cart);

			BufferedWriter writer = new BufferedWriter(
				new FileWriter("output" + count + ".txt"));

			writer.write(printedReceipt);

			writer.close();

			count += 1;
		}
	}

	protected ShoppingCart cart;

}