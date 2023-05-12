const iframe = document.getElementById('ContentFrame');

function hideElements() {
    const doc = iframe.contentDocument;
    const elements = doc.body.querySelectorAll('.new.newpar.section');
    const filteredElements = Array.from(elements).filter(element => {
        return element.classList.length === 3;
    });

    for (const filterElement in filteredElements) {
        filteredElements[filterElement].style.display = 'none';
    }
}

function containerFlexTosBlock() {
    const doc = iframe.contentDocument;
    const elements = doc.body.querySelectorAll('.container.col-equal-33-33-33 > .cmp-container > .aem-Grid, .container.col-left-66-right-33 > .cmp-container > .aem-Grid, .container.col-left-33-right-66 > .cmp-container > .aem-Grid');

    for (const filterElement in elements) {
        elements[filterElement].style.display = 'block';
    }
}

iframe.addEventListener('load', hideElements);
iframe.addEventListener('load', containerFlexToBlock);
