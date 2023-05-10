(function () {
    window.onload = function () {
        const relatedinformation = document.querySelector('.relatedinformation');
        const relatedInfoContainer = document.querySelector('.cmp-related-info__content');
        relatedInfoContainer.children.length > 0 ?
            relatedinformation.classList.add('sectionSeparator') :
            relatedinformation.classList.remove('sectionSeparator');

    };
}());