/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayInput} from '@clayui/form';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import React from 'react';

import {LegacyContext} from './Context';

const LoadingIndicatorMarkup = ({
	children,
	...otherProps
}: React.HTMLAttributes<HTMLDivElement>) => (
	<ClayInput.GroupInsetItem {...otherProps} after>
		<span className="inline-item inline-item-middle">{children}</span>
	</ClayInput.GroupInsetItem>
);

export interface IProps extends React.HTMLAttributes<HTMLDivElement> {

	/**
	 * Div component to render. It can be a one component that will replace the markup.
	 */
	component?: React.ComponentType<any>;
}

const ClayAutocompleteLoadingIndicator = ({
	className,
	component: Component = LoadingIndicatorMarkup,
	...otherProps
}: IProps) => {
	const {onLoadingChange} = React.useContext(LegacyContext);

	React.useEffect(() => {
		onLoadingChange(true);

		return () => {
			onLoadingChange(false);
		};
	}, []);

	return (
		<Component {...otherProps} className={className}>
			<ClayLoadingIndicator small />
		</Component>
	);
};

export default ClayAutocompleteLoadingIndicator;
