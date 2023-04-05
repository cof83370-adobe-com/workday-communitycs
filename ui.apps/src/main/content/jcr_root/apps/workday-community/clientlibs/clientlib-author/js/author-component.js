const iframe = document.getElementById('ContentFrame');

function hideElements() {
    const doc = iframe.contentDocument;
    const elements = doc.body.querySelectorAll('.new.newpar.section');
    const filteredElements = Array.from(elements).filter(element => {
        return element.classList.length === 3 && element.classList.contains('new') && element.classList.contains('newpar') && element.classList.contains('section');
    });

    for (const filterElement of filteredElements) {
        filterElement.style.display = 'none';
    }
}

if (iframe.contentDocument && iframe.contentDocument.readyState === 'complete') {
    hideElements();
} else {
    iframe.addEventListener('load', hideElements);
}
