(function() {
    var tocSelectors = {
        tocToggleIcon:  '[class="cmp-toc__heading-icon"]'
    };
    const container = 'container';
    const aemGridColumn = 'aem-GridColumn--default';

    function toggleTocPanel(show) {
        const leftContainer = document.getElementsByClassName(`${container}__left-rail`);
        const leftContainerPanel = leftContainer.length == 1 ? leftContainer[0] : null;

        const centerContainer = document.getElementsByClassName(`${container}__center`);
        const centerContainerPanel = centerContainer.length == 1 ? centerContainer[0] : null;

        if(leftContainerPanel && centerContainerPanel) {
            if(show) {
                leftContainerPanel.classList.add(`${aemGridColumn}--3`);
                leftContainerPanel.classList.remove(`${aemGridColumn}--1`, 'collapse');

                centerContainerPanel.classList.add(`${aemGridColumn}--6`);
                centerContainerPanel.classList.remove(`${aemGridColumn}--8`);
            } else {
                leftContainerPanel.classList.remove(`${aemGridColumn}--3`);
                leftContainerPanel.classList.add(`${aemGridColumn}--1`, 'collapse');

                centerContainerPanel.classList.remove(`${aemGridColumn}--6`);
                centerContainerPanel.classList.add(`${aemGridColumn}--8`);
            }
        }
    }

    function onDocumentReady() {
        const toggleIcon = document.querySelector(tocSelectors.tocToggleIcon);
        var showPanel = true;
        if(toggleIcon){
            toggleIcon.addEventListener('click', function(){
                showPanel = !showPanel;
                toggleTocPanel(showPanel);
            });
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());
