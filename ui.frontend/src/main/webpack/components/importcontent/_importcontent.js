(function() {
    'use strict';
    var selectors = {
        self:      '[data-cmp-is="importcontent"]'
    };
    
    
    function ImportContent(config) {

        function init(config) {
            config.element.removeAttribute('data-cmp-is');
            document.getElementById('cmp_importcontent_button').onclick = function() {

            var xhr = new XMLHttpRequest();

			// Making our connection  
			var url = '/bin/dataimporter';
			xhr.open('GET', url, true);

			// function execute after request is successful 
			xhr.onreadystatechange = function() {
				if (this.readyState == 4 && this.status == 200) {
					console.log(this.responseText);
				}
			};
			// Sending our request 
			xhr.send();

		};
        }
        if (config && config.element) {
            init(config);
        }
    }
    
   function onDocumentReady() {
        var elements = document.querySelectorAll(selectors.self);
        for (var i = 0; i < elements.length; i++) {
            new ImportContent({ element: elements[i] });
        }

        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
        var body             = document.querySelector('body');
        var observer         = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                // needed for IE
                var nodesArray = [].slice.call(mutation.addedNodes);
                if (nodesArray.length > 0) {
                    nodesArray.forEach(function(addedNode) {
                        if (addedNode.querySelectorAll) {
                            var elementsArray = [].slice.call(addedNode.querySelectorAll(selectors.self));
                            elementsArray.forEach(function(element) {
                                new ImportContent({ element: element });
                            });
                        }
                    });
                }
            });
        });

        observer.observe(body, {
            subtree: true,
            childList: true,
            characterData: true
        });
    }

    if (document.readyState !== 'loading') {
        onDocumentReady();
    } else {
        document.addEventListener('DOMContentLoaded', onDocumentReady);
    }
}());
