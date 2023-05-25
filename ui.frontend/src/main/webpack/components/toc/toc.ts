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
        var firstLevelItems = document.querySelectorAll('.cmp-toc__firstlevelitem-title');
        var acc = [];

        firstLevelItems.forEach(function(item) {
          var parentElement = item.closest('.cmp-toc__item-link');
          if (parentElement) {
            acc.push(parentElement);
          }
        });

        var panel = document.getElementsByClassName('cmp-toc__group cmp-toc__secondlevellist');
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
        var firstLevelItems = document.querySelectorAll('.cmp-toc__secondlevelitem-title');
        var acc = [];

        firstLevelItems.forEach(function(item) {
          var parentElement = item.closest('.cmp-toc__item-link');
          if (parentElement) {
            acc.push(parentElement);
          }
        });


        var panel = document.getElementsByClassName('cmp-toc__group cmp-toc__thirdlevellist');
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

    function addChevronImage() {
        const tocListItems = document.querySelectorAll('.cmp-toc__group li a:has(+ ul)');

        if(tocListItems && tocListItems.length != 0) {
            tocListItems.forEach(function(item) {
                const listChevron = document.createElement('span');
                listChevron.classList.add('cmp-toc__chevron');
                item.appendChild(listChevron);
            });
        }
    }

    function expandCollapseChevron() {
        const tocChevronIcons = document.querySelectorAll('.cmp-toc__chevron');

        if(tocChevronIcons  && tocChevronIcons.length != 0) {
            tocChevronIcons.forEach(icon => {
                icon.addEventListener('click', () => {
                    event.preventDefault();
                });
            });
        }

    }

    function expandActiveBook() {
        var activeItem = document.querySelector('.cmp-toc__item.active');

        if (activeItem) {
            var parentElement = activeItem.parentElement;
            parentElement.classList.add('show');

            var previousSibling = parentElement.previousElementSibling as HTMLElement;
            if (previousSibling) {
                previousSibling.classList.add('active');
            }

            if (parentElement.classList.contains('cmp-toc__thirdlevellist')) {
                var grandparentElement = parentElement.parentElement.parentElement;
                grandparentElement.classList.add('show');

                var grandparentPreviousSibling = grandparentElement.previousElementSibling as HTMLElement;
                if (grandparentPreviousSibling) {
                    grandparentPreviousSibling.classList.add('active');
                }
            }
        }
    }

    function onDocumentReady() {
        const tocClose = document.querySelector(tocSelectors.tocModalClose);
        let showModal = false;
        
        if(showModal) {
            openTocModal();
        } else {
            closeTocModal();
        }

        addChevronImage();
        expandCollapseChevron();
        leftrailpanellevel1();
        leftrailpanellevel2();

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
    window.onload = expandActiveBook;

}());
