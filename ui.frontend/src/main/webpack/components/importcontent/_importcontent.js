(function() {
    'use strict';
    var selectors = {
        self:      '[data-cmp-is="importcontent"]'
    };
    
    
    function ImportContent(config) {

        function init(config) {
            config.element.removeAttribute('data-cmp-is');
            document.getElementById('cmp_importcontent_button').onclick = function() {
            let loaderImage = document.getElementById('report-loader');
            loaderImage.src = '/etc.clientlibs/workday-community/clientlibs/clientlib-site/resources/images/report-loading.gif';
            loaderImage.style.display = 'block';

            var xhr = new XMLHttpRequest();

			// Making our connection  
			var url = '/bin/dataimporter';
			xhr.open('GET', url, true);
            //button style, loading gif

			// function execute after request is successful 
			xhr.onreadystatechange = function() {
                const reportDiv = document.getElementById('migration-report');
				if (this.readyState == 4 && this.status == 200) {
					console.log(this.responseText);
                    const response = JSON.parse(this.responseText);
                    const reportEntries = response.reportItemList;
                    if(reportDiv !== null && reportEntries.length > 0) {
                        const reportTable = document.createElement('table');
                        reportTable.id = 'report';
                        reportDiv.appendChild(reportTable);
                        const td1 = '<td>Input File</td>';
                        const td2 = '<td>Drupal Node ID</td>';
                        const td3 = '<td>Migration Status</td>';
                        const td4 = '<td>Template</td>';
                        const td5 = '<td>Page</td>';
                        let reportHeader = document.createElement('tr');
                        reportHeader.innerHTML = td1+td2+td3+td4+td5;
                        reportTable.appendChild(reportHeader);
                        for (const reportEntry of reportEntries) {
                            const td1 = '<td>'+reportEntry['inputFileName']+'</td>';
                            const td2 = '<td>'+reportEntry['drupalNodeId']+'</td>';
                            const td3 = '<td>'+reportEntry['migrationStatus']+'</td>';
                            const td4 = '<td>'+reportEntry['templateName']+'</td>';
                            const td5 = '<td>'+reportEntry['aemPagePath']+'</td>';
                            let reportRow = document.createElement('tr');
                            reportRow.innerHTML = td1+td2+td3+td4+td5;
                            reportTable.appendChild(reportRow);
                        }
                    }
                    loaderImage.style.display = 'none';
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
