import React from 'react';
import './Header.scss';
import { Header } from '@workday/canvas-kit-labs-react-header';
import { Button } from '@workday/canvas-kit-react-button';

function HeaderComponent(props) {

    return (
        <div className='cmp-header'>
            <Header title='Workday' brandUrl='#'>
                <nav>
                    <ul>
                        <li>
                            <a>{props.listItem1}</a>
                        </li>
                        <li>
                            <a>{props.listItem2}</a>
                        </li>
                        <li>
                            <a>{props.listItem3}</a>
                        </li>
                        <li>
                            <a>{props.listItem4}</a>
                        </li>
                        <li>
                            <a>{props.listItem5}</a>
                        </li>
                    </ul>
                </nav>
                <Button variant={Button.Variant.Primary}>Sign Up</Button>

            </Header>
        </div>
    );
}

export default HeaderComponent;
