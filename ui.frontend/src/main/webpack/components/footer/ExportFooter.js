import FooterComponent from './Footer';
import * as ReactDOMServer from 'react-dom/server';

class ExportHeader extends HTMLElement {
    constructor(){
        super();
        this.render();
    }

    render(){
        const stringTemplate = ReactDOMServer.
        renderToString(<FooterComponent footerItem1="About Us"
        footerItem2="Subscriptions"
        footerItem3="Legal"
        footerItem4="Privacy"/>);
        this.innerHTML = stringTemplate;
    }
}

window.customElements.define("footer-element", ExportHeader);



