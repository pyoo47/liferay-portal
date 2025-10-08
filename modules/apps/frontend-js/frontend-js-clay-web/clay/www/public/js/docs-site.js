/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

if (!Element.prototype.matches) {
	Element.prototype.matches =
		Element.prototype.msMatchesSelector ||
		Element.prototype.webkitMatchesSelector;
}

if (!Element.prototype.closest) {
	Element.prototype.closest = function (selector) {
		let node = this;

		while (node.nodeType === 1) {
			if (node.matches(selector)) {
				return node;
			}

			node = node.parentNode;
		}

		return null;
	};
}

(function () {
	document.addEventListener('click', (event) => {
		const t = event.target;

		const a =
			t.tagName === 'a' || t.tagName === 'button'
				? t
				: t.closest('a') || t.closest('button');
		const column = t.closest('.autofit-col-toggle') || false;

		if (a) {
			if (a.getAttribute('href') === '#1') {
				event.preventDefault();
			}

			const dataToggle = a.getAttribute('data-toggle');

			if (dataToggle && dataToggle.startsWith('c-prefers')) {
				document
					.querySelector('body')
					.classList.toggle(a.getAttribute('data-toggle'));
			}
		}

		if (column && button) {
			var button = column.querySelector('.component-action');

			button.classList.toggle('show');

			document
				.querySelector(button.dataset.target)
				.classList.toggle('show');
		}
	});
})();

(function () {
	function calcProgressWidth(el) {
		const clayRange = el.closest('[data-toggle="clay-css-range"]');

		const min = el.getAttribute('min') || 0;
		const max = el.getAttribute('max') || 100;
		const step = el.getAttribute('step') || 1;

		const thumbWidth = parseInt(
			clayRange.querySelector('.clay-range-progress .clay-range-thumb')
				.offsetWidth
		);
		const rangeWidth = parseInt(el.offsetWidth);

		const currentStep = (el.value - min) / step;
		const totalSteps = (max - min) / step;
		const progressWidth = (currentStep / totalSteps) * 100;

		let offsetWidth = progressWidth;
		const ratio =
			(((1 - progressWidth * 0.01) * (thumbWidth / 1.001)) / rangeWidth) *
			100;

		if (progressWidth !== 50) {
			offsetWidth =
				progressWidth - (thumbWidth / 2 / rangeWidth) * 100 + ratio;
		}

		return offsetWidth;
	}

	function clayRange(e) {
		const clayRangeInput = e.target.closest(
			'[data-toggle="clay-css-range"]'
		);

		if (clayRangeInput) {
			offsetWidth = calcProgressWidth(e.target);

			clayRangeInput.querySelector('.clay-range-progress').style.width =
				offsetWidth + '%';

			const currentVal = e.target.value;

			const title = clayRangeInput.querySelectorAll('.clay-range-value');

			for (let i = 0; i < title.length; i++) {
				title[i].innerHTML = currentVal;
			}
		}
	}

	document.addEventListener('input', clayRange);

	document.addEventListener('change', clayRange);
})();
