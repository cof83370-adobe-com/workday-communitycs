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
          const siblingElement = item.previousElementSibling;
          if (siblingElement && siblingElement.classList.contains('cmp-toc__chevron')) {
            acc.push(siblingElement);
          }
        });

        const panel = document.getElementsByClassName('cmp-toc__group cmp-toc__secondlevellist');
        for (var i = 0; i < acc.length; i++) {
            acc[i].addEventListener('click', function () {
                const setClasses = !this.parentElement.classList.contains('active');
                const accLinks = [];
                acc.forEach(function(item) {
                  const accParent = item.parentElement;
                    accLinks.push(accParent);
                });

                setClass(accLinks, 'active', 'remove');
                setClass(panel, 'show', 'remove');
                if (setClasses) {
                    this.parentElement.classList.toggle('active');
                    if(this.parentElement.nextElementSibling) {
                        this.parentElement.nextElementSibling.classList.toggle('show');
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
          const siblingElement = item.previousElementSibling;
          if (siblingElement && siblingElement.classList.contains('cmp-toc__chevron')) {
            acc.push(siblingElement);
          }
        });


        const panel = document.getElementsByClassName('cmp-toc__group cmp-toc__thirdlevellist');
        for (var i = 0; i < acc.length; i++) {
            acc[i].addEventListener('click', function () {
                const setClasses = !this.parentElement.classList.contains('active');
                const accLinks = [];
                acc.forEach(function(item) {
                  const accParent = item.parentElement;
                    accLinks.push(accParent);
                });

                setClass(accLinks, 'active', 'remove');
                setClass(panel, 'show', 'remove');
                if (setClasses) {
                    this.parentElement.classList.toggle('active');
                    if(this.parentElement.nextElementSibling) {
                        this.parentElement.nextElementSibling.classList.toggle('show');
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
        const tocListItems = document.querySelectorAll('.cmp-toc__group li .cmp-toc__item-link:has(+ ul)');

        if(tocListItems && tocListItems.length != 0) {
            tocListItems.forEach(function(item) {
                const listChevron = document.createElement('button');
                listChevron.classList.add('cmp-toc__chevron');
                listChevron.setAttribute('tabindex', '0');
                listChevron.setAttribute('aria-label', (item as HTMLElement).outerText);
                const firstChild = item.firstChild;
                item.insertBefore(listChevron, firstChild);
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
                    const firstLevelLink = previousSibling.parentElement.parentElement.previousElementSibling;

                    if(firstLevelLink) {
                        firstLevelLink.classList.add('active');
                    }
                }
            }
        }

        const activeItemLinks = document.querySelectorAll('.cmp-toc__item-link.active');
        activeItemLinks.forEach(itemLink => {
            const siblingUl = itemLink.nextElementSibling;
            if (siblingUl && siblingUl.tagName === 'UL') {
                itemLink.children[0].setAttribute('aria-expanded', true.toString());
            }
        });

        const itemLinksWithoutActiveClass = document.querySelectorAll('.cmp-toc__item-link:not(.active)');
        itemLinksWithoutActiveClass.forEach(itemLink => {
            const siblingUl = itemLink.nextElementSibling;
            if (siblingUl && siblingUl.tagName === 'UL') {
                itemLink.children[0].setAttribute('aria-expanded', false.toString());
            }
        });
    }

    function getSelectedTOCItem() {
        const firstLevelItem = document.querySelector('.cmp-toc__item.active .cmp-toc__firstlevelitem-title');
        const secondLevelItem = document.querySelector('.cmp-toc__item.active .cmp-toc__secondlevelitem-title');
        const thirdLevelItem = document.querySelector('.cmp-toc__item.active .cmp-toc__thirdlevelitem-title');
        const activeItem = firstLevelItem || secondLevelItem || thirdLevelItem;

        if (activeItem) {
            const visuallyHiddenText = document.createElement('span');
            visuallyHiddenText.textContent = '(selected)';
            visuallyHiddenText.classList.add('visually-hidden');
            activeItem.appendChild(visuallyHiddenText);
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

    window.addEventListener('load', () => {
      expandActiveBook();
      getSelectedTOCItem();
    });

}());
