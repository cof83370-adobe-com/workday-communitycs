(function() {

    function applyColEqual33Styles(config) {
        const elements = config.getElementsByClassName('container');
        for (var i = 0; i < elements.length && elements[i]; i++) {
            elements[i].classList.add('col33');
            if(i == 1) {
                elements[i].classList.add('spacing-left', 'spacing-right');
            }
        }
    }

    function applyCol33_66Styles(config) {
        const elements = config.getElementsByClassName('container');
        if(elements && elements.length == 2) {
            elements[0].classList.add('col33');
            elements[1].classList.add('col66', 'spacing-left');
        }
    }

    function applyCol66_33Styles(config) {
        const elements = config.getElementsByClassName('container');
        if(elements && elements.length == 2) {
            elements[0].classList.add('col66', 'spacing-right');
            elements[1].classList.add('col33');
        }
    }

    function onDocumentReady() {
        const colEqual33 = document.getElementsByClassName('col-equal-33-33-33');
        const colEqual33Element = colEqual33.length == 1 ? colEqual33[0] : null;
        if(colEqual33Element) {
            applyColEqual33Styles(colEqual33Element);
        }

        const col33_66 = document.getElementsByClassName('col-left-33-right-66');
        const col33_66Element = col33_66.length == 1 ? col33_66[0] : null;
        if(col33_66Element) {
           applyCol33_66Styles(col33_66Element);
        }

        const col66_33 = document.getElementsByClassName('col-left-66-right-33');
        const col66_33Element = col66_33.length == 1 ? col66_33[0] : null;
        if(col66_33Element) {
           applyCol66_33Styles(col66_33Element);
        }
   }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());
