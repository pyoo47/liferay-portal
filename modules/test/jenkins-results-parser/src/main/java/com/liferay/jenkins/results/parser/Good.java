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

import java.util.StringTokenizer;

/**
 * @author Yi-Chen Tsai
 */

public class Good {

    public Good(String goodDescriptor) {

        _importedTaxRate = 0.05;
        _taxRate = 0.1;

        _imported = goodDescriptor.contains("imported");

        _exempt = goodDescriptor.contains("book") ||
                goodDescriptor.contains("chocolate") ||
                goodDescriptor.contains("pills");

        StringTokenizer item_token = new StringTokenizer(goodDescriptor);

        int item_token_count = item_token.countTokens();

        double price = 0.0;
        int quantity = 0;
        String goodName = "";

        for (int i = 0; i < item_token_count; i++) {
            if (i == 0) {
                quantity = Integer.parseInt(item_token.nextToken());
            }
            else if (i == (item_token_count - 1)) {
                price = Double.parseDouble(item_token.nextToken());
            }
            else {
                String tok = item_token.nextToken();

                if (!tok.contentEquals("at")) {
                    if (goodName.isEmpty()) {
                        goodName += tok;
                    }
                    else {
                        goodName += " " + tok;
                    }
                }
            }
        }

        _name = goodName;
        _price = price;
    }

    public double getFinalPrice() {
        return getPrice() + getTax();
    }

    public String getName() {
        return _name;
    }

    public double getPrice() {
        return _price;
    }

    public double getTax() {
        return Math.ceil(
                _price *
                        getTaxRate() * 20.0) / 20.0;
    }

    protected double getTaxRate(){
        double taxRate = 0.0;
        if (_imported){
            taxRate += _importedTaxRate;
        }

        if (!_exempt){
            taxRate += _taxRate;
        }

        return taxRate;
    }

    private final boolean _exempt;
    private final boolean _imported;
    private final double _importedTaxRate;
    private final String _name;
    private final double _price;
    private final double _taxRate;

}
