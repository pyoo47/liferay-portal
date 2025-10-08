/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

'use client';

/**
 * SPDX-FileCopyrightText: © 2018 Liferay, Inc. <https://liferay.com>
 * SPDX-License-Identifier: BSD-3-Clause
 */

import React, {useEffect} from 'react';

export default (props) => {
	const SwatchColorRef = React.useRef();

	const [hex, setHex] = React.useState(props.hex);
	const [rgb, setRgb] = React.useState(props.rgb);

	const updateValues = () => {
		const computedStyles = window.getComputedStyle(SwatchColorRef.current);

		setHex(computedStyles.getPropertyValue('content').replace(/"/g, ''));

		setRgb(computedStyles.getPropertyValue('background-color'));
	};

	useEffect(() => {
		const handleChange = () => {
			updateValues();
		};

		updateValues();

		document.addEventListener('colorsLoaded', handleChange, false);

		return () => {
			document.removeEventListener('colorsLoaded', handleChange, false);
		};
	}, []);

	return (
		<div className="clay-site-swatch">
			<div
				className={`clay-site-swatch-color clay-site-swatch-${props.varName.substring(
					1
				)}`}
				ref={SwatchColorRef}
			/>

			<div className="clay-site-swatch-body">
				<div className="clay-site-swatch-title">
					{props.lexiconName}
				</div>

				<div className="autofit-row">
					<div className="autofit-col clay-site-swatch-type">Var</div>

					<div className="autofit-col autofit-col-expand clay-site-swatch-value">
						<span className="clay-site-swatch-text clay-site-var">
							{props.varName}
						</span>
					</div>
				</div>

				<div className="autofit-row">
					<div className="autofit-col clay-site-swatch-type">Hex</div>

					<div className="autofit-col autofit-col-expand clay-site-swatch-value">
						<span className="clay-site-hex clay-site-swatch-text">
							{hex}
						</span>
					</div>
				</div>

				<div className="autofit-row">
					<div className="autofit-col clay-site-swatch-type">Rgb</div>

					<div className="autofit-col autofit-col-expand clay-site-swatch-value">
						<span className="clay-site-rgb clay-site-swatch-text">
							{rgb}
						</span>
					</div>
				</div>
			</div>
		</div>
	);
};
