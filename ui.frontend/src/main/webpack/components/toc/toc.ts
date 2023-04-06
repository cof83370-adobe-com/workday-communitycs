(function() {
    const toc = 'cmp-toc';
    const tocSelectors = {
        tocModalButton:  '[class="cmp-button"]',
        tocModalClose:  `[class="${toc}__modal-close"]`
    };
    const tocModal = document.getElementsByClassName(`${toc}__modal`);
    const tocModalContainer = tocModal.length == 1 ? tocModal[0] : null;
    const tocButton = document.querySelector(tocSelectors.tocModalButton);

    function openTocModal() {
        if (tocModalContainer !== null) {
            tocButton.classList.add('hide-modal');
            tocModalContainer.classList.remove('hide-modal');
        }
    }

    function closeTocModal() {
        if (tocModalContainer !== null) {
            tocModalContainer.classList.add('hide-modal');
            tocButton.classList.remove('hide-modal');
        }
    }

    function leftrailpanellevel1() {
        var acc = document.getElementsByClassName('cmp-toc__item-link');
        var panel = document.getElementsByClassName('cmp-toc__group-1');
        for (var i = 0; i < acc.length; i++) {
            acc[i].addEventListener('click', function () {
                var setClasses = !this.classList.contains('active');
                setClass(acc, 'active', 'remove');
                setClass(panel, 'show', 'remove');
                if (setClasses) {
                    this.classList.toggle('active');
                    this.nextElementSibling.classList.toggle('show');
                }
            });
        }
        function setClass(els, className, fnName) {
            for (var i = 0; i < els.length; i++) {
                els[i].classList[fnName](className);
            }
        }

    }

    function leftrailpanellevel2() {
        var acc = document.getElementsByClassName('cmp-toc__item-link-1');
        var panel = document.getElementsByClassName('cmp-toc__group-2');
        for (var i = 0; i < acc.length; i++) {
            acc[i].addEventListener('click', function () {
                var setClasses = !this.classList.contains('active');
                setClass(acc, 'active', 'remove');
                setClass(panel, 'show', 'remove');
                if (setClasses) {
                    this.classList.toggle('active');
                    this.nextElementSibling.classList.toggle('show');
                }
            })
        }
        function setClass(els, className, fnName) {
            for (var i = 0; i < els.length; i++) {
                els[i].classList[fnName](className);
            }
        }
    }

    function onDocumentReady() {
        const tocClose = document.querySelector(tocSelectors.tocModalClose);
        let showModal = false;
        leftrailpanellevel1();
        leftrailpanellevel2();
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