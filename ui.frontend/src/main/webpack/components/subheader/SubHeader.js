import React from 'react';
import './SubHeader.scss';
import {PageHeader} from '@workday/canvas-kit-react-page-header';
import {IconButton} from '@workday/canvas-kit-react-button';
import {exportIcon, fullscreenIcon} from '@workday/canvas-system-icons-web';

function SubHeaderComponent(props) {
    console.log("the props", props);
    return (
        <div>
            <PageHeader title="Product">
                <IconButton icon={exportIcon} />
                <IconButton icon={fullscreenIcon} />
            </PageHeader>
            <PageHeader title="With Cap Width" capWidth={true}>
                <IconButton icon={exportIcon} />
                <IconButton icon={fullscreenIcon} />
            </PageHeader>
        </div>
    );
}

export default SubHeaderComponent;
