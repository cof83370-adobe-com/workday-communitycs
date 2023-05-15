(function() {
    const applyStyle = (elems: HTMLCollection, x: number, y?: number) => {
       if (!elems || elems.length !== 1) {return;}
       const elements = elems[0].getElementsByClassName('container');
     
       if (!y) {
         // Handling 33-33-33 column styles
         for (var i = 0; i < elements.length && elements[i]; i++) {
            elements[i].classList.add(`col${x}`);
         }
       } else if(elements && elements.length == 2) {
         // Handling 66-33 and 33-66 column styles
          if (x < y) {
             elements[0].classList.add(`col${x}`);
          } else if (x > y) {
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
