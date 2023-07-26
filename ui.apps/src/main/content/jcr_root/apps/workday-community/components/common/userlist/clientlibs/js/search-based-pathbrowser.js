(function () {
    const WORKDAY_PREFIX = "workday.granite.ui.search.pathBrowser",
        ROOT_PATH = "rootPath",
        WRAPPER_CLASS = ".workday-search-pathbrowser-wrapper-ourmUser",
        QUERY = "/bin/ourmUsers?";

    //executed when user initiates search in pathbrowser by typing in a keyword
    function searchBasedAutocompleteCallback() {
        return {
            name: WORKDAY_PREFIX + '.autocompletecallback',
            handler: autoCompleteHandler
        };

        function autoCompleteHandler(searchTerm) {
            const self = this, deferred = $.Deferred();
            let $inputEle = self.$element.parents(WRAPPER_CLASS).siblings(".manualOverride").find('input[type="checkbox"]');

            if (_.isEmpty(searchTerm)) {
                return;
            }

            const searchParams = getSearchParameters(self, searchTerm);

            if (_.size(searchTerm) > 2 && !$inputEle.prop("checked")) {
                self.optionLoader(searchParams, callback);
            }

            function callback(results) {
                if (_.isEmpty(results)) {
                    deferred.resolve([]);
                    return;
                }

                self.options.options = results;
                deferred.resolve(_.range(results.length));
            }

            return deferred.promise();
        }

        function getSearchParameters(widget, searchTerm) {
            const searchParams = {
                searchText: searchTerm
            };

            const path = widget.$element.data(ROOT_PATH);

            if (!_.isEmpty(path)) {
                searchParams.path = path;
            }

            return searchParams;
        }
    }

    CUI.PathBrowser.register('autocompleteCallback', searchBasedAutocompleteCallback());

    //the option loader for requesting query results
    function searchBasedOptionLoader() {
        return {
            name: WORKDAY_PREFIX + ".optionLoader",
            handler: optionLoaderHandler
        };

        function optionLoaderHandler(searchParams, callback) {
            let query = QUERY;

            _.each(searchParams, function (value, key) {
                query = query + key + "=" + value + "&";
            });

            query = query.substring(0, query.length - 1);

            $.get(query).done(handler);

            function handler(data) {
                let results = [];

                if (!_.isEmpty(JSON.parse(data.hits))) {
                    results = JSON.parse(data.hits)['users'];
                }

                if (callback) {
                    callback(results);
                }
            }

            return false;
        }
    }

    CUI.PathBrowser.register('optionLoader', searchBasedOptionLoader());

    //option renderer for creating the option html
    function searchBasedOptionRenderer() {
        return {
            name: WORKDAY_PREFIX + ".optionRenderer",
            handler: optionRendererHandler
        };

        function optionRendererHandler(iterator, index) {
            let value = this.options.options[index];
            let email = value.email;
            let username = value.username;
            let fullName = `${value.firstName} ${value.lastName}`;
            let profileImageData = value.profileImageData;

            return $(`<li class="coral-SelectList-item ourmUserLi coral-SelectList-item--option" data-profile-image-data="${profileImageData}" data-value="${fullName}">${email}</li>`);
        }
    }

    CUI.PathBrowser.register('optionRenderer', searchBasedOptionRenderer());
}());