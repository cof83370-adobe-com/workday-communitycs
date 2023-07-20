const iframe = document.getElementById('ContentFrame');

function hideElements() {
    const doc = iframe.contentDocument;
    const elements = doc.body.querySelectorAll('.new.newpar.section');
    const filteredElements = Array.from(elements).filter(element => {
        return element.classList.length === 3;
    });

    filteredElements.forEach(function (filterElement, index) {
        filterElement.style.display = 'none';
    });
}

iframe.addEventListener('load', hideElements);
