/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jethr0.entity.dalo;

import com.liferay.client.extension.util.spring.boot.LiferayOAuth2AccessTokenManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Michael Hashimoto
 */
@Configuration
public abstract class BaseDALO {

	protected String getAuthorization() {
		return _liferayOAuth2AccessTokenManager.getAuthorization(
				_liferayHeadlessServerOAuthApplicationExternalReferenceCode);
	}

	protected void refresh() {
		_liferayOAuth2AccessTokenManager.refresh(
			_liferayHeadlessServerOAuthApplicationExternalReferenceCode);
	}

	@Value("${liferay.headless.server.oauth.application.external.reference.code}")
	private String _liferayHeadlessServerOAuthApplicationExternalReferenceCode;

	@Autowired
	private LiferayOAuth2AccessTokenManager _liferayOAuth2AccessTokenManager;

}