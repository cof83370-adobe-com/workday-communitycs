(function() {
    const applyStyle = (elems: HTMLCollection, x: number, y?: number) => {
       if (!elems || elems.length !== 1) {return;}
       const elements = elems[0].getElementsByClassName('container');

       if (!y) {
         for (var i = 0; i < elements.length && elements[i]; i++) {
            elements[i].classList.add(`col${x}`);
            if(i == 1) {
                elements[i].classList.add('spacing-left', 'spacing-right');
            }
         }
       } else if(elements && elements.length == 2) {
          if (x < y) {
             elements[0].classList.add(`col${x}`);
             elements[1].classList.add(`col${y}`, 'spacing-left');
          } else if (x > y) {
             elements[0].classList.add(`col${x}`, 'spacing-right');
             elements[1].classList.add(`col${y}`);
          }
       }
    };

    document.addEventListener('DOMContentLoaded', () => {
        applyStyle(document.getElementsByClassName('col-equal-33-33-33'), 33);
        applyStyle(document.getElementsByClassName('col-left-33-right-66'), 33, 66);
        applyStyle(document.getElementsByClassName('col-left-66-right-33'), 66, 33);
    });
}());
