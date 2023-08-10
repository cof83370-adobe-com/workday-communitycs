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
        const firstLevelItems = document.querySelectorAll('.cmp-toc__firstlevelitem-title');
        const acc = [];

        firstLevelItems.forEach(function(item) {
          const parentElement = item.closest('.cmp-toc__item-link');
          if (parentElement) {
            acc.push(parentElement);
          }
        });

        const panel = document.getElementsByClassName('cmp-toc__group cmp-toc__secondlevellist');
        for (var i = 0; i < acc.length; i++) {
            acc[i].addEventListener('click', function () {
                const setClasses = !this.classList.contains('active');
                setClass(acc, 'active', 'remove');
                setClass(panel, 'show', 'remove');
                if (setClasses) {
                    this.classList.toggle('active');
                    if(this.nextElementSibling) {
                        this.nextElementSibling.classList.toggle('show');
                        this.setAttribute('aria-expanded', true);
                    }
                } else {
                    this.setAttribute('aria-expanded', false);
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
        const firstLevelItems = document.querySelectorAll('.cmp-toc__secondlevelitem-title');
        const acc = [];

        firstLevelItems.forEach(function(item) {
          const parentElement = item.closest('.cmp-toc__item-link');
          if (parentElement) {
            acc.push(parentElement);
          }
        });


        const panel = document.getElementsByClassName('cmp-toc__group cmp-toc__thirdlevellist');
        for (var i = 0; i < acc.length; i++) {
            acc[i].addEventListener('click', function () {
                const setClasses = !this.classList.contains('active');
                setClass(acc, 'active', 'remove');
                setClass(panel, 'show', 'remove');
                if (setClasses) {
                    this.classList.toggle('active');
                    if(this.nextElementSibling) {
                        this.nextElementSibling.classList.toggle('show');
                        this.setAttribute('aria-expanded', true);
                    }
                } else {
                    this.setAttribute('aria-expanded', false);
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
        const firstLevelItem = document.querySelector('.cmp-toc__group.cmp-toc__firstlevellist > .cmp-toc__item.active');

        if (firstLevelItem) {
            const firstLevelItemLink = firstLevelItem.querySelector('.cmp-toc__item-link');
            firstLevelItemLink.classList.add('active');

            const firstLevelUl = firstLevelItem.querySelector('ul');
            if(firstLevelUl) {
                firstLevelUl.classList.add('show');
            }
        } else {
            const secondLevelItem = document.querySelector('.cmp-toc__group.cmp-toc__secondlevellist > .cmp-toc__item.active');
            const thirdLevelItem = document.querySelector('.cmp-toc__group.cmp-toc__thirdlevellist > .cmp-toc__item.active');

            if (secondLevelItem) {
                const secondLevelItemLink = secondLevelItem.querySelector('.cmp-toc__item-link');
                secondLevelItemLink.classList.add('active');
                secondLevelItem.parentElement.classList.add('show');

                const previousSibling = secondLevelItem.parentElement.previousElementSibling;
                if (previousSibling) {
                    previousSibling.classList.add('active');
                }

                const secondLevelUl = secondLevelItem.querySelector('ul');
                if(secondLevelUl) {
                    secondLevelUl.classList.add('show');
                }
            } else if (thirdLevelItem) {
                thirdLevelItem.parentElement.classList.add('show');
                const previousSibling = thirdLevelItem.parentElement.previousElementSibling;
                if (previousSibling) {
                    previousSibling.classList.add('active');
                    previousSibling.parentElement.parentElement.classList.add('show');
                }
            }
        }

        const activeItemLinks = document.querySelectorAll('.cmp-toc__item-link.active');
        activeItemLinks.forEach(itemLink => {
            const siblingUl = itemLink.nextElementSibling;
            if (siblingUl && siblingUl.tagName === 'UL') {
                itemLink.setAttribute('aria-expanded', true.toString());
            }
        });

        const itemLinksWithoutActiveClass = document.querySelectorAll('.cmp-toc__item-link:not(.active)');
        itemLinksWithoutActiveClass.forEach(itemLink => {
            const siblingUl = itemLink.nextElementSibling;
            if (siblingUl && siblingUl.tagName === 'UL') {
                itemLink.setAttribute('aria-expanded', false.toString());
            }
        });
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
