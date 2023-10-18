(function() {
    const image = 'cmp-image';
    const imageSelectors = {
        imageElement: `[class="${image}"]`,
        expandOption: '[class="expand-media"]'
    };
    let isModalOpen = false;

    function addExpandImageOption(config: any) {
        const expandLink = document.createElement('a');
        expandLink.textContent = 'Expand Image';
        expandLink.className = 'expand-media';
        const imageElements = config.element.children;
        for (var i = 0; i < imageElements.length && imageElements[i]; i++) {
            if(imageElements[i].className == `${image}__image`) {
                if(imageElements[i].alt) {
                    expandLink.ariaLabel = `expand image of ${imageElements[i].alt} to full size`;
                } else {
                    expandLink.ariaLabel = 'expand image to full size';
                }
            }
        }
        config.element.append(expandLink);
    }

    function expandImage(option) {
        const parent = option.expandElement.parentElement.children;
        for (var i = 0; i < parent.length && parent[i]; i++) {
            if(parent[i].className == `${image}__image`) {
                const img = parent[i];
                addImageModal(img);
            }
        }
    }

    function addImageModal(img) {
        const modalDiv = document.createElement('div');
        modalDiv.className = `${image}__modal`;

        const closeModal = document.createElement('span');
        closeModal.className = `${image}__close`;
        closeModal.setAttribute('tabindex', '0');
        closeModal.setAttribute('aria-label', 'Close expanded image');

        const modalContent = document.createElement('img');
        modalContent.className = `${image}__modal-content`;

        modalDiv.append(closeModal, modalContent);
        img.parentNode.appendChild(modalDiv);

        const modal = img.parentNode.getElementsByClassName(`${image}__modal`);
        const imgModal = modal.length == 1 ? modal[0] : null;

        const imgModalContent = img.parentNode.getElementsByClassName(`${image}__modal-content`);
        const imgModalContentItem = imgModalContent.length == 1 ? imgModalContent[0] : null;
        imgModal.style.display = 'block';
        imgModalContentItem.src = img.src;
        isModalOpen = true;

        const spanClose = img.parentNode.getElementsByClassName(`${image}__close`)[0];

        spanClose.addEventListener('click', closeImageModal);

        spanClose.addEventListener('keydown', function (event) {
            if (event.key === 'Enter' || event.keyCode === 13) {
                closeImageModal();
            }
        });

        if(isModalOpen == true) {
            document.addEventListener('keydown', function (event) {
                if (isModalOpen && event.key === 'Escape') {
                    closeImageModal();
                }
            });
        }

        function closeImageModal() {
            imgModal.style.display = 'none';
            const modalToRemove = img.parentNode.querySelector('.cmp-image__modal');
            if (modalToRemove) {
                modalToRemove.remove();
            }
            isModalOpen = false;
            img.parentElement.querySelector('.expand-media').focus();
        }
    }

    function onDocumentReady() {
        const elements = document.querySelectorAll(imageSelectors.imageElement);
        for (var i = 0; i < elements.length && elements[i]; i++) {
            if(elements[i].parentElement.classList.contains('enable-expand')) {
                addExpandImageOption({ element: elements[i] });
            }
        }

        const expandElements = document.querySelectorAll(imageSelectors.expandOption);
        expandElements.forEach(function(expandElement) {
            expandElement.addEventListener('click', function(e) {
                expandImage({ expandElement });
            });

            expandElement.setAttribute('href', '#');
        });
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

    window.addEventListener('load', function() {
        const expandElements = document.querySelectorAll(imageSelectors.expandOption);
        expandElements.forEach(function(expandElement) {
            expandElement.addEventListener('click', function(e) {
                expandImage({ expandElement });
            });
        });
    });

}());
