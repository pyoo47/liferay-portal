/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.clay.web.internal.js.importmaps.extender;

import com.liferay.frontend.js.importmaps.extender.JSImportMapsContributor;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.petra.string.StringBundler;

import jakarta.servlet.ServletContext;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryce Osterhaus
 */
@Component(service = JSImportMapsContributor.class)
public class FrontendJSClayWebJSImportMapsContributor
	implements JSImportMapsContributor {

	@Override
	public JSONObject getImportMapsJSONObject() {
		return _importMapsJSONObject;
	}

	@Activate
	protected void activate() {
		_importMapsJSONObject = _jsonFactory.createJSONObject();

		for (String moduleName : _MODULE_NAMES) {
			_importMapsJSONObject.put(
				moduleName,
				StringBundler.concat(
					_servletContext.getContextPath(), "/__liferay__/exports/",
					moduleName.replaceAll("\\/", "\\$"), ".js"));
		}
	}

	private static final String[] _MODULE_NAMES = {
		"@clayui/alert",
		"@clayui/autocomplete",
		"@clayui/badge",
		"@clayui/breadcrumb",
		"@clayui/button",
		"@clayui/card",
		"@clayui/charts",
		"@clayui/color-picker",
		"@clayui/core",
		"@clayui/data-provider",
		"@clayui/date-picker",
		"@clayui/drop-down",
		"@clayui/empty-state",
		"@clayui/form",
		"@clayui/icon",
		"@clayui/label",
		"@clayui/layout",
		"@clayui/link",
		"@clayui/list",
		"@clayui/loading-indicator",
		"@clayui/localized-input",
		"@clayui/management-toolbar",
		"@clayui/modal",
		"@clayui/multi-select",
		"@clayui/multi-step-nav",
		"@clayui/nav",
		"@clayui/navigation-bar",
		"@clayui/pagination",
		"@clayui/pagination-bar",
		"@clayui/panel",
		"@clayui/popover",
		"@clayui/progress-bar",
		"@clayui/provider",
		"@clayui/shared",
		"@clayui/slider",
		"@clayui/sticker",
		"@clayui/table",
		"@clayui/tabs",
		"@clayui/time-picker",
		"@clayui/toolbar",
		"@clayui/tooltip",
		"@clayui/upper-toolbar",
	};

	private JSONObject _importMapsJSONObject;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.frontend.js.clay.web)",
		unbind = "-"
	)
	private ServletContext _servletContext;

}