const container = document.querySelector('.cmp-tabs');
const primary = container.querySelector('.cmp-tabs__tablist');
const primaryItems = container.querySelectorAll('.cmp-tabs__tablist > li:not(.-more)');
const visibleItems = container.querySelectorAll('.cmp-tabs__tablist > li:not(.--hidden)');

// insert "more" button and duplicate the list

primary.insertAdjacentHTML('beforeend', `<li class="-more"><button type="button" aria-haspopup="true" aria-expanded="false">More</button><ul class="-secondary">${primary.innerHTML}</ul></li>`);
const secondary = container.querySelector('.-secondary');
const secondaryItems = secondary.querySelectorAll('li');
const allItems = container.querySelectorAll('li');
const moreLi = primary.querySelector('.-more');
const moreBtn = moreLi.querySelector('button');
moreBtn.addEventListener('click', (e) => {
    e.preventDefault();
    container.classList.toggle('--show-secondary');
});
//Hide Secondary on click  on visible tabs
visibleItems.forEach(el => el.addEventListener('click', event => {
    event.preventDefault();
    if (container.classList.contains('--show-secondary')) {
        container.classList.toggle('--show-secondary');
    }
}));


// adapt tabs

const doAdapt = () => {
    // reveal all items for the calculation
    allItems.forEach((item) => {
        item.classList.remove('--hidden');
    })

    // hide items that won't fit in the Primary
    let stopWidth = moreBtn.offsetWidth;
    const hiddenItems = [];

    const primaryWidth = primary.clientWidth;

    primaryItems.forEach((item, i) => {

        if (primaryWidth >= stopWidth + item.clientWidth) {
            stopWidth += item.clientWidth;
        } else {
            item.classList.add('--hidden');
            hiddenItems.push(i);
        }

    });


    // toggle the visibility of More button and items in Secondary
    if (!hiddenItems.length) {
        moreLi.classList.add('--hidden');
        container.classList.remove('--show-secondary');
        moreBtn.setAttribute('aria-expanded', 'false');
    }
    else {
        secondaryItems.forEach((item, i) => {
            if (!hiddenItems.includes(i)) {
                item.classList.add('--hidden');
            }
        })
    }
}

doAdapt(); // adapt immediately on load
window.addEventListener('resize', doAdapt); // adapt on window resize