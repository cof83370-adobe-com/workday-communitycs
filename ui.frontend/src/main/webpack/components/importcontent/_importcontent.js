(function() {
    'use strict';
    var selectors = {
        self:      '[data-cmp-is="importcontent"]',
        checkbox:  '[data-cmp-hook-importcontent="checkbox"]'
    };
    
    
    function ImportContent(config) {

        function init(config) {
            config.element.removeAttribute('data-cmp-is');
            document.getElementById('cmp_importcontent_button').onclick = function() {
            let loaderImage = document.getElementById('report-loader');
            loaderImage.src = '/etc.clientlibs/workday-community/clientlibs/clientlib-site/resources/images/report-loading.gif';
            loaderImage.style.display = 'block';

            var checkbox = config.element.querySelectorAll(selectors.checkbox);
            checkbox = checkbox.length == 1 ? checkbox[0].checked : null;

            var xhr = new XMLHttpRequest();

			// Making our connection  
            var url = '/bin/dataimporter?overWriteEnabled='+checkbox;
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
                        //Process Summary section
                        const statusDiv = document.getElementById('migration-status');
                        const statusHeader = '<div class="cmp-title__text">Migration Status Summary</div><div class="status-summary">';
                        const statusSpan1 = '<div class="status">Total processed: '+response.totalCount+'</div>';
                        const statusSpan2 = '<div class="status">Created with content: '+response.createdCount+'</div>';
                        const statusSpan3 = '<div class="status">Replaced with new content: '+response.replacedCount+'</div>';
                        const statusSpan4 = '<div class="status">Page already exists: '+response.existingCount+'</div>';
                        const statusSpan5 = '<div class="status red">No mapping found: '+response.noMappingCount+'</div>';
                        const statusSpan6 = '<div class="status red">Template validation failed: '+response.templateValidationCount+'</div>';
                        const statusSpan7 = '<div class="status red">Page creation failed: '+response.pageCreationFailed+'</div>';
                        const statusSpan8 = '<div class="status red">Error occured: '+response.errorCount+'</div>';
                        const summaryCloseDiv = '</div>';
                        if(statusDiv !== null){
                            statusDiv.innerHTML  = statusHeader+
                            statusSpan1+statusSpan2+statusSpan3+statusSpan4+
                            statusSpan5+statusSpan6+statusSpan7+statusSpan8+summaryCloseDiv;
                        }

                        //Process report table section
                        const reportTable = document.createElement('table');
                        reportTable.id = 'report';
                        reportDiv.innerHTML='';
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
                            //TODO: Make page clickable, add download report, table bottom padding
                            const td5 = '<td>'+reportEntry['aemPagePath']+'</td>';
                            let reportRow = document.createElement('tr');
                            reportRow.innerHTML = td1+td2+td3+td4+td5;
                            reportTable.appendChild(reportRow);
                        }
                    } else if (reportDiv !== null) {
                        reportDiv.innerHTML = '<h3><span>No report available. Check input files.</span></h3>';
                    }
                    loaderImage.style.display = 'none';
				} else if (this.readyState == 4 && this.status == 500) {
                    if (reportDiv !== null) {
                        reportDiv.innerHTML = '<h3><span>Error occured during migration. Check error log.</span></h3>';
                    }
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
