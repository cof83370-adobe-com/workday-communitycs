import React from 'react';

function CardComponent(props){
    console.log("Inside react card function");
    console.log("the props", props);

    // props = {
    //     heading: 'heading',
    //     description: 'desc',
    //     theme: 'light-blue'
    // }

    console.log("the props1:::", props);

    function getBackgroundColor(){
        if(props.theme == 'dark-blue'){
            return 'darkblue';
        } else if(props.theme == 'light-blue'){
            return 'lightblue';
        } else if(props.theme == 'dark-grey'){
            return 'darkgrey';
        } else if(props.theme == 'light-grey'){
            return 'lightgrey';
        }
    }

    return (
        <div class="cmp-card">
            <div class="cmp-card__content">
                <h2 class="cmp-card__heading"> {props.heading} </h2>
                <div class="cmp-card__description" style={{ backgroundColor: getBackgroundColor()}}>
                    <p dangerouslySetInnerHTML={{__html: props.description}}></p>
                </div>
            </div>
        </div>
    );
}
export default CardComponent;
