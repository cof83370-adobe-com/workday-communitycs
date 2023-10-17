(function() {

    function onDocumentReady() {
        const dynamicElements = document.querySelectorAll('[class*="wd-limited-access__"]');

        if (dynamicElements.length > 0) {
            for (var i = 0; i < dynamicElements.length; i++) {
                const element = dynamicElements[i];
                const badge = document.createElement('span');
                badge.className = 'wd-limited-access__badge';
                badge.textContent = 'LIMITED ACCESS';

                if (element.firstChild) {
                    element.insertBefore(badge, element.firstChild);
                } else {
                    element.appendChild(badge);
                }
            }
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());
