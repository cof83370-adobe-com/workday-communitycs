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
        if (!headerData) {
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
                searchProps: { redirectPath: searchURL }
            };

            if (stringValid(homePage)) {
                headerData.homeUrl = homePage;
            }

            headerData = JSON.stringify(headerData);
            sessionStorage.setItem('navigation-data', headerData);

            // Although the digitalData set at window object, we set it in session scope.
            let dataLayer = headerDiv.getAttribute('data-cmp-data-layer');
            if (dataLayer) {
              let dataLayerObj = JSON.parse(dataLayer);
              window.digitalData = dataLayerObj.digitalData;
            }
        }

        headerData = JSON.parse(headerData);
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
