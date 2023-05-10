(function () {
    window.onload = function () {
        const taxonomyContainer = document.querySelector('.taxonomy');
        const taxonomyListContainer = document.querySelector('.cmp-taxonomy');
        taxonomyListContainer.children.length > 0 ? taxonomyContainer.classList.add('sectionSeparator')
            : taxonomyContainer.classList.remove('sectionSeparator');
    }
}());