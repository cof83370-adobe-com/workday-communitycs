function renderNavFooter() {
    const elementCreator = React.createElement;

    const footerDiv = document.getElementById('community-footer-div');

    if (stringValid(footerDiv)) {
        const footerData = {
            ...Cmty.DefaultFooterData,
            context: {
                // Below is the Drupal Adobe Analytics Dev URL. 
                // TODO: this needs to be updated to Community AEM URL and read from OSGi config.
                cookiePrefUrl: 'https://assets.adobedtm.com/d0845c7cd69d/2c847d99b6d2/launch-cd3b9e540274-development.min.js',
            }
        };
        const footerElement = elementCreator(Cmty.GlobalFooter, { menus: footerData });
        ReactDOM.render(footerElement, footerDiv);
    }
}

document.addEventListener('readystatechange', event => {
    if (event.target.readyState === 'complete') {
        renderNavFooter();
    }
});
