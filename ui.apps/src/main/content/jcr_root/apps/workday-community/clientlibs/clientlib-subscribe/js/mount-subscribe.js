(function(react, reactDom) {
    const SUBSCRIBE_API_PATH = '/bin/subscribe';
    const SUBSCRIBE_CREATE_API_PATH = '/bin/subscribe/create';

    const ERROR_MSG_FETCH = 'We are unable to fetch the page\'s subscription status. Please try again later.';
    const ERROR_MSG_CREATE = 'We are unable to save your subscription at this time. Please try again later.';
    function renderSubscribe() {
        const subscribeDiv = document.getElementById('community-subscribe-div');
        if (subscribeDiv) {
            const manageUrl = subscribeDiv.getAttribute('data-manage-url');
            const title = subscribeDiv.getAttribute('data-title');
            const height = subscribeDiv.getAttribute('data-height');
            const width = subscribeDiv.getAttribute('data-width');
            const subscribeText = subscribeDiv.getAttribute('data-subscribe-text');
            const manageText = subscribeDiv.getAttribute('data-manage-text');
            const errorMessage = subscribeDiv.getAttribute('data-error-message');
            const data = {};

            if (manageUrl) data['manageUrl'] = manageUrl;
            if (manageText) data['manageText'] = manageText;
            if (title) data['title'] = title;
            if (subscribeText) data['subscribeText'] = subscribeText;
            if (height) data['height'] = height;
            if (width) data['width'] = width;
            if (errorMessage) data['errorMsg'] = errorMessage;

            // implement callback.
            data['callback'] = (subscribe) => {
                if (!subscribe) {
                    return fetch(
                        SUBSCRIBE_API_PATH
                    ).then((response) => response.json())
                     .then((result) => result? ({status: result['subscribed']}) : ({ status: false }))
                     .catch((reason) => ({status: false, message: ERROR_MSG_FETCH}))
                }

                return fetch(SUBSCRIBE_CREATE_API_PATH)
                    .then((response) => response.json())
                    .then((result) => result && result['subscribed']? ({status: result['subscribed']}) : ({ status: false, message: ERROR_MSG_CREATE }))
                    .catch((reason) => ({status: false, message: ERROR_MSG_CREATE}))
            };

            const subscribeElem = react.createElement(Cmty.Subscribe, data);
            reactDom.render(subscribeElem, subscribeDiv);
        }
    }

    document.addEventListener('readystatechange', event => {
        if (event.target.readyState === 'complete') {
            renderSubscribe();
        }
    });
})(React, ReactDOM);
