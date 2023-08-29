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
        let cacheChanged = headerDiv.getAttribute('data-cache-property');
        cacheChanged = !cacheChanged && cacheChanged === 'CHANGED';
        if (!headerData || headerData && cacheChanged) {
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

            headerData = {
                menus: headerMenu,
                skipTo: 'mainContentId',
                sticky: true,
                searchProps: { redirectPath: searchURL, querySeparator: '#', queryParameterName: 'q' }
            };

            if (stringValid(homePage)) {
                headerData.homeUrl = homePage;
            }

            if (dataWithMenu(headerData)) {
                sessionStorage.setItem('navigation-data', headerData);
            } else {
                document.cookie = 'cacheMenu=FALSE';
                sessionStorage.removeItem('navigation-data');
            }

            headerData = JSON.stringify(headerData);
            // Although the digitalData set at window object, we set it in session scope.
            let dataLayer = headerDiv.getAttribute('data-cmp-data-layer');
            if (dataLayer) {
              let dataLayerObj = JSON.parse(dataLayer);
              window.digitalData = dataLayerObj.digitalData;
            }
        }

        try {
            let headerDataJson = JSON.parse(headerData);
            if (dataWithMenu(headerDataJson)) {
                sessionStorage.setItem('navigation-data', headerData);
            } else {
                document.cookie = 'cacheMenu=FALSE';
                sessionStorage.removeItem('navigation-data');
            }

            const headerElement = React.createElement(Cmty.GlobalHeader, headerDataJson);
            ReactDOM.render(headerElement, headerDiv);
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
