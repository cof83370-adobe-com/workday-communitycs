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

let searchConfig;

const getSearchToken = new Promise((resolve, reject) => {
    const tokenUrl = '/bin/search/token';
    try {
        fetch(tokenUrl).then(response => response.json())
            .then(res => {
                if (!res && !res['searchToken']) {
                    reject('failed to getToken');
                } else {
                    resolve(res['searchToken']);
                }
            });
    } catch (err) {
        return reject('failed to getToken');
    }
});

function renderNavHeader() {
    const headerDiv = document.getElementById('community-header-div');
    if (headerDiv !== undefined && headerDiv !== null) {
        let enableCache = headerDiv.getAttribute('data-enable-cache');
        let headerData = sessionStorage.getItem('navigation-data');
        let headerDataJson = headerData ? JSON.parse(headerData) : null;
        let previousId = headerDataJson ? headerDataJson['previousId'] : null;
        let currentId = headerDiv.getAttribute('data-cache-property');
        let dataModel = headerDiv.getAttribute('data-model-property');
        let changed = currentId !== previousId;

        if (!headerData || headerData && changed) {
            headerDataJson = constructData(headerDiv, currentId);
        }

        if (dataWithMenu(headerDataJson) && (enableCache === 'true')) {
            sessionStorage.setItem('navigation-data', JSON.stringify(headerDataJson));
        } else {
            document.cookie = 'cacheMenu=FALSE';
            sessionStorage.removeItem('navigation-data');
            headerDataJson = constructData(headerDiv, currentId);
        }
        // coveoProp can't be cached. move it out.
        const searchUrl = headerDiv.getAttribute('data-search-url');
        searchConfig = JSON.parse(headerDiv.getAttribute('data-search-config'));

        const unAuthenticated = {
            redirectPath: searchUrl,
            querySeparator: '#',
            queryParameterName: 'q'
        };

        headerDataJson.coveoProps = (dataModel === 'HIDE_MENU_UNAUTHENTICATED') ? undefined : () => getSearchToken.then((searchToken) => (
            {
                engine: Cmty.CoveoEngineService.CoveoSearchEngine(
                    {
                        organizationId: searchConfig['orgId'],
                        search: {
                            searchHub: searchConfig['searchHub']
                        },
                        accessToken: searchToken,
                        renewAccessToken: () => getSearchToken
                    }
                ),
                controllerConfig: {
                    numberOfSuggestions: 10
                },
                redirectProps: {
                    redirectPath: searchUrl,
                    querySeparator: '#',
                    queryParameterName: 'q'
                },
                analytics: {
                    analyticsClientMiddleware: (eventName, payload) => {
                        const userContext = searchConfig["userContext"]
                        if (userContext) {
                            payload.customData = {...payload.customData, ...JSON.parse(userContext)};
                        }
                        return payload;
                    }
                }
            }
        )).catch((error) => {
            if (dataModel !== 'HIDE_MENU_UNAUTHENTICATED') {
                // Add search props if Coveo props is not present and not public page
                headerDataJson.searchProps = { ...unAuthenticated };
            } else {
                headerDataJson.searchProps = undefined;
            }
        });

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

    let headerData = {
        previousId: currentId,
        menus: undefined,
        skipTo: 'mainContentId',
        sticky: true,
    };
    if (stringValid(headerStringData) && headerStringData !== 'HIDE_MENU_UNAUTHENTICATED') {
        let headerMenu = JSON.parse(headerStringData);
        if (headerMenu.unAuthenticated === undefined || headerMenu.unAuthenticated === false) {
            if (!headerMenu.profile) {
                headerMenu.profile = [];
            }

            if (stringValid(avatarUrl)) {
                headerMenu.profile.avatar = {...headerMenu.profile.avatar, data: avatarUrl};
            }

            headerMenu.profile.menu = [...headerMenu.profile.menu, signOutObject];
            headerData.menus = headerMenu;
        }
    }


    if (stringValid(homePage)) {
        headerData.homeUrl = homePage;
    }

    return headerData;
}
