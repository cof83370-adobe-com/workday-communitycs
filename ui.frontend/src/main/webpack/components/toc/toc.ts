(function() {
    const toc = 'cmp-toc';
    var tocSelectors = {
        tocModalButton:  '[class="cmp-button"]',
        tocModalClose:  `[class="${toc}__modal-close"]`
    };
    const tocModal = document.getElementsByClassName(`${toc}__modal`);
    const tocModalContainer = tocModal.length == 1 ? tocModal[0] : null;
    const tocButton = document.querySelector(tocSelectors.tocModalButton);

    function openTocModal() {
        tocButton.classList.add('hide-modal');
        tocModalContainer.classList.remove('hide-modal');
    }

    function closeTocModal() {
        tocModalContainer.classList.add('hide-modal');
        tocButton.classList.remove('hide-modal');
    }

    function onDocumentReady() {
        const tocClose = document.querySelector(tocSelectors.tocModalClose);
        var showModal = false;

        if(showModal) {
            openTocModal();
        } else {
            closeTocModal();
        }

        if(tocButton) {
            tocButton.addEventListener('click', function(){
                showModal = !showModal;
                openTocModal();
            });
        }

        if(tocClose) {
            tocClose.addEventListener('click', function(){
                showModal = !showModal;
                closeTocModal();
            });
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());