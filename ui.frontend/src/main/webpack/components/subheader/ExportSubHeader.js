import SubHeaderComponent from './SubHeader';
import * as ReactDOMServer from 'react-dom/server';

class ExportSubHeader extends HTMLElement {
    constructor(){
        super();
        this.render();
    }

    render(){
        const stringTemplate = ReactDOMServer.
        renderToString(<SubHeaderComponent listItem1="Basics"
        listItem2="Release"
        listItem3="Products"
        listItem4="Collaborate"
        listItem5="Services"/>);
        this.innerHTML = stringTemplate;
    }
}

window.customElements.define("subheader-element", ExportSubHeader);


