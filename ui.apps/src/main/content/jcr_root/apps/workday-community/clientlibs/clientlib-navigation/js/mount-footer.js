function renderNavFooter() {
    const elementCreator = React.createElement;

    const footerDiv = document.getElementById('community-footer-div');
    if (footerDiv !== undefined && footerDiv !== null) {
        let adobeUri = footerDiv.getAttribute('data-model-adobe');
        const footerData = {
            ...Cmty.DefaultFooterData,
            context: {
                cookiePrefUrl: adobeUri,
            }
        };
        const footerElement = elementCreator(Cmty.GlobalFooter, {menus: footerData});
        ReactDOM.render(footerElement, footerDiv);
    }
}

document.addEventListener('readystatechange', event => {
    if (event.target.readyState === 'complete') {
        renderNavFooter();
    }
});
