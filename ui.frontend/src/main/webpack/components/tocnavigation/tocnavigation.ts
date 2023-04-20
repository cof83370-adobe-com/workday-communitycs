(function() {
    const tocGroup = document.getElementsByClassName('cmp-toc__group');
    const tocList = tocGroup.length == 1 ? tocGroup[0] : null;

    const prevBtn = document.getElementById('toc-previous') as HTMLButtonElement;
    const nextBtn = document.getElementById('toc-next') as HTMLButtonElement;

    function loadLinkForCurrentItem(currentIndex) {
        const currentItem = tocList.children[parseInt(currentIndex, 10)];
        const link = currentItem.querySelector('a');
        window.location.href = link.href;
    }

    function updateButtonState(currentIndex): void {
        if (parseInt(currentIndex, 10) === 0) {
            prevBtn.disabled = true;
            prevBtn.classList.add('btn-disable');
        } else {
            prevBtn.disabled = false;
            prevBtn.classList.remove('btn-disable');
        }
        if (parseInt(currentIndex, 10) === tocList.children.length - 1) {
            nextBtn.disabled = true;
            nextBtn.classList.add('btn-disable');
        } else {
            nextBtn.disabled = false;
            nextBtn.classList.remove('btn-disable');
        }
    }

    function getActiveItem() {
        const currentUrl = window.location.href;
        const links = document.querySelectorAll('.cmp-toc__item a');
        let activeIndex;
        for (var i = 0; i < links.length; i++) {
            const linkUrl = links[i].getAttribute('href');

            if (currentUrl.indexOf(linkUrl) !== -1) {
                const liElement = links[i].parentNode as HTMLElement;
                liElement.classList.add('active');
                activeIndex = Array.prototype.indexOf.call(liElement.parentNode.children, liElement);
                localStorage.setItem('activeIndex', activeIndex);
                break;
            }
        }
        updateButtonState(activeIndex);
    }

    function traverseLiItems(currentIndex, direction) {
        let nextIndex;

        if (direction === 'next') {
            nextIndex = parseInt(currentIndex, 10) + 1;
        } else {
            nextIndex = parseInt(currentIndex, 10) - 1;
        }

        loadLinkForCurrentItem(nextIndex);
    }


    function onDocumentReady() {
        if(tocGroup) {
            getActiveItem();
            const currentIndex = localStorage.getItem('activeIndex') || '0';

            prevBtn.addEventListener('click', () => {
                traverseLiItems(currentIndex, 'prev');
            });

            nextBtn.addEventListener('click', () => {
                traverseLiItems(currentIndex, 'next');
            });
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());

