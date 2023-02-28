(function() {
    var containerSelectors = {
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

    function checkTocPanel() {
        const leftContainer = document.getElementsByClassName(`${container}__left-rail`);
        const leftContainerPanel = leftContainer.length == 1 ? leftContainer[0] : null;

        const centerContainer = document.getElementsByClassName(`${container}__center`);
        const centerContainerPanel = centerContainer.length == 1 ? centerContainer[0] : null;

        const toc = document.getElementsByClassName('cmp-toc');
        const tocElement = toc.length == 1 ? toc[0] : null;

        const tocGroup = document.getElementsByClassName('cmp-toc__group');

        if(tocGroup.length == 0) {
            leftContainerPanel.classList.remove(`${aemGridColumn}--3`, `${aemGridColumn}--1`, 'collapse');
            leftContainerPanel.classList.add(`${aemGridColumn}--0`);
            if(tocElement) {
                tocElement.classList.add('hide');
            }

            centerContainerPanel.classList.remove(`${aemGridColumn}--6`, `${aemGridColumn}--8`);
            centerContainerPanel.classList.add(`${aemGridColumn}--9`);
        }
    }

    function onDocumentReady() {
        const toggleIcon = document.querySelector(containerSelectors.tocToggleIcon);
        var showPanel = true;
        checkTocPanel();

        if(toggleIcon){
            toggleIcon.addEventListener('click', function(){
                showPanel = !showPanel;
                toggleTocPanel(showPanel);
            });
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());
