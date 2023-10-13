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

let searchToken;

const getSearchToken = async () => {
    const tokenUrl = '/bin/search/token';
    try {
        const response = await fetch(tokenUrl);
        const res = await response.json();
        return res['searchToken'];
    } catch (err) {
        return '';
    }
};

function renderNavHeader() {
    const headerDiv = document.getElementById('community-header-div');
    if (headerDiv !== undefined && headerDiv !== null) {
        let headerData = sessionStorage.getItem('navigation-data');
        let headerDataJson = headerData ? JSON.parse(headerData) : null;
        let previousId = headerDataJson ? headerDataJson['previousId'] : null;
        let currentId = headerDiv.getAttribute('data-cache-property');
        let searchUrl = headerDiv.getAttribute('data-search-url');
        let coveoOrgId = headerDiv.getAttribute('data-org-id');
        let coveoSearchHub = headerDiv.getAttribute('data-search-hub');
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

        headerDataJson.coveoProps = {
            engine: Cmty.CoveoEngineService.CoveoSearchEngine(
                {
                    organizationId: coveoOrgId,
                    search: {
                        searchHub: coveoSearchHub
                    },
                    accessToken: searchToken,
                    renewAccessToken: getSearchToken
                }
            ),
            controllerConfig: {
                numberOfSuggestions: 10
            },
            redirectProps: {
                redirectPath: searchUrl,
                querySeparator: '#',
                queryParameterName: 'q'
            }
        };

        try {
            const headerElement = React.createElement(Cmty.GlobalHeader, headerDataJson);
            ReactDOM.render(headerElement, headerDiv);

            // Set adobe data on window.digitalData property.
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

document.addEventListener('readystatechange', async (event) => {
    if (event.target.readyState === 'complete') {
        searchToken = await getSearchToken();
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
    let headerMenu;
    if (stringValid(headerStringData) && headerStringData !== 'HIDE_MENU_UNAUTHENTICATED') {
        headerMenu = JSON.parse(headerStringData);
        if (headerMenu.unAuthenticated === undefined || headerMenu.unAuthenticated === false) {
            if (!headerMenu.profile) {
                headerMenu.profile = [];
            }

            if (stringValid(avatarUrl)) {
                headerMenu.profile.avatar = {...headerMenu.profile.avatar, data: avatarUrl};
            }

            headerMenu.profile.menu = [...headerMenu.profile.menu, signOutObject];
        }
    }

    let headerData = {
        previousId: currentId,
        menus: headerMenu,
        skipTo: 'mainContentId',
        sticky: true,
    };

    if (stringValid(homePage)) {
        headerData.homeUrl = homePage;
    }

    return headerData;
}
