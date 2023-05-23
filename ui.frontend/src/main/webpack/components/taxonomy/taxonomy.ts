(function () {
    window.onload = function () {
        const taxonomyContainer = document.querySelector('.taxonomy');
        const taxonomyListContainer = document.querySelector('.cmp-taxonomy');
        if (taxonomyContainer !== null) {
            taxonomyListContainer.children.length > 0 ? taxonomyContainer.classList.add('sectionSeparator')
                : taxonomyContainer.classList.remove('sectionSeparator');
        }
    };
}());
