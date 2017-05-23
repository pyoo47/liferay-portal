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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Yi-Chen Tsai
 */

public class ShoppingCart {

    public ShoppingCart() {
        _goods = new LinkedHashMap<>();
    }

    public void addGood(String goodDescriptor) {
        _goods.put(new Good(goodDescriptor), 0);
    }

    public void printReceipt() {
        double salesTaxes = 0.0;
        double total = 0.0;

        for (Map.Entry<Good, Integer> entry: _goods.entrySet()) {
            Good good = entry.getKey();
            int goodQuantity = entry.getValue();

            double goodFinalPrice = goodQuantity * good.getFinalPrice();

            salesTaxes += goodQuantity * good.getTax();

            total += goodFinalPrice;

            System.out.println(
                    "1" + " " + good.getName() + ": " +
                            String.format("%.2f", goodFinalPrice));
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Sales Taxes: ");
        sb.append(String.format("%.2f", salesTaxes));
        sb.append("\n");
        sb.append("Total: ");
        sb.append(String.format("%.2f", total));
        sb.append("\n");

        System.out.println(sb.toString());
    }

    private final Map<Good, Integer> _goods;

}