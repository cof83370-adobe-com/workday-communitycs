(function() {

function authorPageEdit() {
    const editableContainers = document.querySelectorAll('div.cq-Overlay--component[data-type="Editable"]');

    const observer = new MutationObserver(mutations => {
        mutations.forEach(mutation => {
            if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                const targetElement = mutation.target;
                if (targetElement.classList.contains('is-hover', 'is-selected', 'is-active')) {
                    if(targetElement.title != 'Image') {
                        const configureButton = document.querySelector('button[data-action="CONFIGURE"]');
                        if (configureButton) {
                            configureButton.addEventListener("click", loadNewDOM);
                        }
                    }
                }
            }
        });
    });

    editableContainers.forEach(container => {
        observer.observe(container, { attributes: true });
    });
}

function loadNewDOM() {
    setTimeout(getDynamicElement, 2000);
}

function getDynamicElement() {
    const browseImgLink = document.querySelector('.coral-Link.cq-FileUpload-browse');
    if(browseImgLink) {
        browseImgLink.style.pointerEvents = 'none';
        browseImgLink.style.color = 'gray';
    }
}

window.onload = authorPageEdit;
}());
