const iframe = document.getElementById('ContentFrame');

function hideElements() {
    const doc = iframe.contentDocument;
    const elements = doc.body.querySelectorAll('.new.newpar.section');
    const filteredElements = Array.from(elements).filter(element => {
        return element.classList.length === 3;
    });

    for (const filterElement of filteredElements) {
        filterElement.style.display = 'none';
    }
}

iframe.addEventListener('load', hideElements);
