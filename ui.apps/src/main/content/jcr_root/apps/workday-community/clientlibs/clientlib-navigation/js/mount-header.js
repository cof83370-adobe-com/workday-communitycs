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
        let headerStringData = headerDiv.getAttribute('data-model-property');
        let avatarUrl = headerDiv.getAttribute("data-model-avatar");
        let homePage = headerDiv.getAttribute("data-prop-home");
        let dataLayer = headerDiv.getAttribute('data-cmp-data-layer');
        let searchURL = headerDiv.getAttribute('data-search-url');
        if (dataLayer) {
            let dataLayerObj = JSON.parse(dataLayer);
            window.digitalData = dataLayerObj.digitalData;
        }

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

        const headerData = {
            menus: headerMenu,
            skipTo: 'mainContentId',
            sticky: true,
            searchProps: { redirectPath: searchURL, querySeparator: '#', queryParameterName: 'q' }
        };

        if (stringValid(homePage)) {
            headerData.homeUrl = homePage;
        }

        const headerElement = React.createElement(Cmty.GlobalHeader, headerData);
        ReactDOM.render(headerElement, headerDiv);
    }
}

document.addEventListener('readystatechange', event => {
    if (event.target.readyState === 'complete') {
        renderNavHeader();
    }
});

function stringValid(str) {
    return (str !== undefined && str !== null && str.trim() !== '');
}
