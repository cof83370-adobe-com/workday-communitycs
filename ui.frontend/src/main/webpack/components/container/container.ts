(function() {
    var tocSelectors = {
        tocToggleIcon:  '[class="cmp-toc__heading-icon"]'
    };

    function toggleTocPanel(show) {
        const leftContainer = document.getElementsByClassName('container__left-rail');
        const leftContainerPanel = leftContainer.length == 1 ? leftContainer[0] : null;

        const centerContainer = document.getElementsByClassName('container__center');
        const centerContainerPanel = centerContainer.length == 1 ? centerContainer[0] : null;

        if(show) {
            leftContainerPanel.classList.add('aem-GridColumn--default--3');
            leftContainerPanel.classList.remove('aem-GridColumn--default--1', 'collapse');

            centerContainerPanel.classList.add('aem-GridColumn--default--6');
            centerContainerPanel.classList.remove('aem-GridColumn--default--8');
        } else {
            leftContainerPanel.classList.remove('aem-GridColumn--default--3');
            leftContainerPanel.classList.add('aem-GridColumn--default--1', 'collapse');

            centerContainerPanel.classList.remove('aem-GridColumn--default--6');
            centerContainerPanel.classList.add('aem-GridColumn--default--8');
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
