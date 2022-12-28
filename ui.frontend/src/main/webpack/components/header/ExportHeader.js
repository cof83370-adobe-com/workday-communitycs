import HeaderComponent from './Header';
import * as ReactDOMServer from 'react-dom/server';

class ExportHeader extends HTMLElement {
    constructor(){
        super();
        this.render();
    }

    render(){
        const stringTemplate = ReactDOMServer.
        renderToString(<HeaderComponent listItem1="Basics"
        listItem2="Release"
        listItem3="Products"
        listItem4="Collaborate"
        listItem5="Services"/>);
        this.innerHTML = stringTemplate;
    }
}

window.customElements.define("header-element", ExportHeader);



