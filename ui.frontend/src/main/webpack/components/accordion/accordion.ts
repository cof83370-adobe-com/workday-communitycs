(function() {
    var accordionSelectors = {
        item:      '[class="cmp-accordion__item"]',
        panel:  '[data-cmp-hook-accordion="panel"]',
        button:   '[data-cmp-hook-accordion="button"]',
        header: '[class="cmp-accordion__header"]'
    };

    function addCollapseButton(config: any) {

        if (config && config.element) {
            init(config);
        }

        function init(config) {

            let panel = config.element.querySelectorAll(accordionSelectors.panel);
            panel = panel.length == 1 ? panel[0] : null;

            let header = config.element.querySelectorAll(accordionSelectors.header);
            header = header.length == 1 ? header[0] : null;

            const collapseButton = document.createElement('a');
            collapseButton.href = '#';
            collapseButton.innerText = 'Collapse';
            collapseButton.classList.add('collapse-button');
            collapseButton.addEventListener('click', function() {
                event.preventDefault();
                let button = header.querySelectorAll(accordionSelectors.button);
                button = button.length == 1 ? button[0] : null;
                button.click();
              });
            panel.appendChild(collapseButton);
        }

    }

    function onDocumentReady() {
        const elements = document.querySelectorAll(accordionSelectors.item);
        for (var i = 0; i < elements.length; i++) {
            new addCollapseButton({ element: elements[i] });
        }
    }

    if (document.readyState !== 'loading') {
        onDocumentReady();
    } else {
        document.addEventListener('DOMContentLoaded', onDocumentReady);
    }

}());
