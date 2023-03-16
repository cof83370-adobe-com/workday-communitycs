(function() {
    const image = 'cmp-image';
    const imageSelectors = {
        imageElement: `[class="${image}"]`,
        expandOption: '[class="expand-media"]'
    };

    function addExpandImageOption(config: any) {
        const expandSpan = document.createElement('span');
        expandSpan.textContent = 'Expand Image';
        expandSpan.className = 'expand-media';
        config.element.append(expandSpan);
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

        const spanClose = img.parentNode.getElementsByClassName(`${image}__close`)[0];

        spanClose.addEventListener('click', function(){
            imgModal.style.display = 'none';
            img.parentNode.removeChild(modalDiv);
        });
    }

    function onDocumentReady() {
        const elements = document.querySelectorAll(imageSelectors.imageElement);
        for (var i = 0; i < elements.length && elements[i]; i++) {
            if(elements[i].parentElement.classList.contains('enable-expand')) {
                addExpandImageOption({ element: elements[i] });
            }
        }

        window.onclick = e => {
            const expandElements = document.querySelectorAll(imageSelectors.expandOption);
            for (var i = 0; i < expandElements.length && expandElements[i]; i++) {
                if(e.target == expandElements[i]) {
                    expandImage({ expandElement: expandElements[i] });
                }
            }
        };
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());
