import React from 'react';
import './Header.scss';
import { Header } from '@workday/canvas-kit-labs-react-header';
// import { IconButton } from '@workday/canvas-kit-react-button';
// import { notificationsIcon } from '@workday/canvas-system-icons-web';
import { Button } from '@workday/canvas-kit-react-button';

// import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';
// import 'react-tabs/style/react-tabs.css';

function HeaderComponent(props) {
    console.log("the props", props);

    // function handleClick(){
    //     // this.setState({msg : 'Welcome to the React world!'})
    //     // document.body.style.backgroundColor = "green";
    // }

    function getUserData() {
        fetch(
            "https://jsonplaceholder.typicode.com/posts")
            .then((res) => res.json())
            .then((jsonData) => {
                console.log(jsonData);
                console.log("Data title:::::", jsonData[0].title);
                console.log("Data body:::::", jsonData[0].body);
            })
    }

    return (
        <div className='cmp-header'>
            <Header title="Workday" brandUrl="#">
                <nav>
                    <ul>
                        <li>
                            <a>{props.listItem1}</a>
                            {/* <a onClick={handleClick}>Home</a> */}
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
                <Button variant={Button.Variant.Primary} onClick={getUserData}>Sign Up</Button>

            </Header>
        </div>
    );
}

export default HeaderComponent;
