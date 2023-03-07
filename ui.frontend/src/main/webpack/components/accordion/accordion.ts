(function() {
	var accordionSelectors = {
		item: '[class="cmp-accordion__item"]',
		panel: '[data-cmp-hook-accordion="panel"]',
		button: '[data-cmp-hook-accordion="button"]',
		header: '[class="cmp-accordion__header"]'
	};

	function addCollapseButton(config: any) {
		let panel = config.element.getElementsByClassName('cmp-accordion__panel');
		panel = panel.length == 1 ? panel[0] : null;
		let header = config.element.getElementsByClassName('cmp-accordion__header');
		header = header.length == 1 ? header[0] : null;
		if (panel.getAttribute('data-collpase-title')) {
			const collapseButton = document.createElement('a');
			collapseButton.href = '#';
			collapseButton.innerText = panel.getAttribute('data-collpase-title');
			collapseButton.classList.add('collapse-button');
			collapseButton.addEventListener('click', function() {
				event.preventDefault();
				let button = header.getElementsByClassName('cmp-accordion__button');
				button = button.length == 1 ? button[0] : null;
				button.click();
			});
			panel.appendChild(collapseButton);
		}
	}

	function onDocumentReady() {
		const elements = document.querySelectorAll(accordionSelectors.item);
		for (var i = 0; i < elements.length && elements[i]; i++) {
			addCollapseButton({ element: elements[i] });
		}
	}

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());
