/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

declare module 'dom-align' {
	export interface IConfig {
		points: readonly [string, string];
		offset: readonly [number, number];
		targetOffset: readonly [string, string];
		overflow: {adjustX: boolean; adjustY: boolean};
	}

	export interface IConfigOptional {
		points?: readonly [string, string];
		offset?: readonly [number, number];
		targetOffset?: readonly [string, string];
		overflow?: {adjustX: boolean; adjustY: boolean};
		useCssRight?: boolean;
	}

	type MousePosition = {
		clientX: number;
		clientY: number;
	};

	export function alignPoint(
		sourceNode: HTMLElement,
		target: MousePosition,
		config?: IConfigOptional
	): void;

	export default function doAlign(
		sourceNode: HTMLElement,
		targetNode: HTMLElement,
		config?: IConfigOptional
	): IConfig;
}
