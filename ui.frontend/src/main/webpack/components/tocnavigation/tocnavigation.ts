(function() {
    const tocList = document.querySelectorAll('.cmp-toc__group li');
    const prevBtn = document.getElementById('toc-previous') as HTMLButtonElement;
    const nextBtn = document.getElementById('toc-next') as HTMLButtonElement;
    const tocNav = document.getElementById('toc-navigation');

    function loadLinkForCurrentItem(currentIndex) {
        const currentItem = tocList[currentIndex];
        const link = currentItem.querySelector('a');
        window.location.href = link.href;
    }

    function updateButtonState(currentIndex) {
        if (parseInt(currentIndex, 10) === 0) {
            prevBtn.disabled = true;
            prevBtn.classList.add('btn-disable');
        } else {
            prevBtn.disabled = false;
            prevBtn.classList.remove('btn-disable');
        }
        if (parseInt(currentIndex, 10) === tocList.length - 1) {
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
                tocNav.style.display = 'block';
                const liElement = links[i].parentNode as HTMLElement;
                liElement.classList.add('active');
                activeIndex = Array.from(tocList).indexOf(liElement as HTMLLIElement);
                localStorage.setItem('activeIndex', activeIndex);
                break;
            } else if (tocNav) {
                tocNav.style.display = 'none';
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
        if(tocList && tocList.length !== 0) {
            getActiveItem();
            const currentIndex = localStorage.getItem('activeIndex') || '0';

            const tocItemLinks = document.getElementsByClassName('cmp-toc__item-link') as HTMLCollectionOf<HTMLElement>;

            for (let i = 0; i < tocItemLinks.length; i++) {
                tocItemLinks[i].addEventListener('click', function(event) {
                    const parentElement = this.parentElement;
                    if (parentElement.classList.contains('cmp-toc__item') && parentElement.classList.contains('active')) {
                        event.preventDefault();
                    }
                });
            }

            prevBtn.addEventListener('click', () => {
                traverseLiItems(currentIndex, 'prev');
            });

            nextBtn.addEventListener('click', () => {
                traverseLiItems(currentIndex, 'next');
            });
        } else if (tocNav) {
            tocNav.style.display = 'none';
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());

