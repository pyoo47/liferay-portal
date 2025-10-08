/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {GoogleAnalytics} from '@next/third-parties/google';
import {Inter} from 'next/font/google';
import Script from 'next/script';

import {SandPackCSS} from './_components/SandpackStyles';

import './clay.scss';

import './globals.scss';

import type {Metadata} from 'next';

const inter = Inter({subsets: ['latin']});

export const metadata: Metadata = {
	metadataBase: new URL('https://clayui.com'),
	title: 'Clay by Liferay',
	description:
		'This is Clay. A web implementation of the Lexicon Experience Language; built by Liferay.',
};

export default function RootLayout({
	children,
}: Readonly<{
	children: React.ReactNode;
}>) {
	return (
		<html lang="en">
			<head>
				<SandPackCSS />

				<Script src="/js/docs-site.js" />
			</head>

			<body className={inter.className}>{children}</body>

			<GoogleAnalytics gaId={process.env.GA4!} />
		</html>
	);
}
