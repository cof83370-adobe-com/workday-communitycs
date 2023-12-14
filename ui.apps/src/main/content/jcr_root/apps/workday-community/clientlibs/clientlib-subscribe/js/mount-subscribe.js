(function(react, reactDom) {
    const SUBSCRIBE_API_PATH = '/bin/subscribe';
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
                    ).then(((response) => response? ({status: response['subscribed']}) : ({ status: false })))
                     .catch((reason) => ({status: false}))
                }

                return fetch(SUBSCRIBE_API_PATH, {
                    method: "POST",
                    credentials: "same-origin",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    referrerPolicy: "no-referrer",
                    body: JSON.stringify("")})
                    .then(((response) => response? ({status: response['subscribed']}) : ({ status: false })))
                    .catch((reason) => ({status: false}))
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
