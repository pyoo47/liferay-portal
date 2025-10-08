/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {visit} from 'unist-util-visit';

const splice = [].splice;

export function unwrapMdxBlockElements() {
	return function transform(tree) {
		visit(tree, visitor);
	};
}

function visitor(node, index, parent) {
	if (Array.isArray(node.children)) {
		node.children.forEach((child) => {
			visit(child, visitor);
		});
	}

	if (
		node.type === 'paragraph' &&
		parent &&
		parent.type === 'mdxJsxFlowElement'
	) {
		splice.apply(parent.children, [index, 1].concat(node.children));

		return [visit.SKIP, index];
	}
}
