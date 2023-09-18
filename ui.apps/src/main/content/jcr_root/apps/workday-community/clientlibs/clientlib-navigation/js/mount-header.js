const signOutObject = {
    type: "Profile",
    tooltip: 'Sign Out',
    level: 1,
    id: 8,
    title: 'Sign Out',
    icon: 'signoutIcon',
    order: 170,
    description: 'Where user unauthenticated platform',
    href: '/bin/user/logout',
    children: [],
};

function renderNavHeader() {
    const headerDiv = document.getElementById('community-header-div');
    if (headerDiv !== undefined && headerDiv !== null) {
        let headerData = sessionStorage.getItem('navigation-data');
        let headerDataJson = headerData ? JSON.parse(headerData) : null;
        let previousId = headerDataJson ? headerDataJson['previousId'] : null;
        let currentId = headerDiv.getAttribute('data-cache-property');
        let changed = currentId !== previousId;

        if (!headerData || headerData && changed) {
            headerDataJson = constructData(headerDiv, currentId);
        }

        if (dataWithMenu(headerDataJson)) {
            sessionStorage.setItem('navigation-data', JSON.stringify(headerDataJson));
        } else {
            document.cookie = 'cacheMenu=FALSE';
            sessionStorage.removeItem('navigation-data');
            headerDataJson = constructData(headerDiv, currentId);
        }

        try {
            const headerElement = React.createElement(Cmty.GlobalHeader, headerDataJson);
            ReactDOM.render(headerElement, headerDiv);
            // Get the adobe data and bind to digitalData property of window object.
            let dataLayer = headerDiv.getAttribute('data-cmp-data-layer');
            if (dataLayer) {
                let dataLayerObj = JSON.parse(dataLayer);
                window.digitalData = dataLayerObj.digitalData;
            }
        } catch (e) {
            document.cookie = 'cacheMenu=FALSE';
            sessionStorage.removeItem('navigation-data');
        }
    }
}

function dataWithMenu(headerData) {
    if (!headerData) return undefined;
    let menus = headerData['menus'];
    if (!menus) return undefined;
    let primary = menus['primary'];
    if (!primary) return undefined;
    let menu = primary['menu'];
    if (!menu || menu.length === 0) return undefined;

    return headerData;
}

document.addEventListener('readystatechange', event => {
    if (event.target.readyState === 'complete') {
        renderNavHeader();
    }
});

function stringValid(str) {
    return (str !== undefined && str !== null && str.trim() !== '');
}

function constructData(headerDiv, currentId) {
    let headerStringData = headerDiv.getAttribute('data-model-property');
    let avatarUrl = headerDiv.getAttribute("data-model-avatar");
    let homePage = headerDiv.getAttribute("data-prop-home");
    let searchURL = headerDiv.getAttribute('data-search-url');

    let headerMenu;
    if (stringValid(headerStringData)) {
        headerMenu = JSON.parse(headerStringData);

        if (!headerMenu.profile) {
            headerMenu.profile = [];
        }

        if (stringValid(avatarUrl)) {
            headerMenu.profile.avatar = { ...headerMenu.profile.avatar, data: avatarUrl };
        }

        headerMenu.profile.menu = [...headerMenu.profile.menu, signOutObject];
    }

    let headerData = {
        previousId: currentId,
        menus: headerMenu,
        skipTo: 'mainContentId',
        sticky: true,
        searchProps: { redirectPath: searchURL, querySeparator: '#', queryParameterName: 'q' }
    };

    if (stringValid(homePage)) {
        headerData.homeUrl = homePage;
    }

    return headerData;
}
