import CardComponent from './Card.js';
import * as ReactDOMServer from 'react-dom/server';

class ExportCard extends HTMLElement {
    constructor(){
        super();
        this.render();
    }

    render(){
        const stringTemplate = ReactDOMServer.
        renderToString(<CardComponent description={this.getAttribute("data-description")}
        heading={this.getAttribute("data-heading")}
        theme={this.getAttribute("data-theme")}/>);
        this.innerHTML = stringTemplate;
    }
}

window.customElements.define("card-element", ExportCard);


