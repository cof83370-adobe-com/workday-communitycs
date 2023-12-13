(function ($, $document) {
    const WORKDAY_PREFIX = "workday.granite.ui.search.pathBrowser",
        WRAPPER_CLASS = ".workday-search-pathbrowser-wrapper-author"
        FORM_FIELD_WRAPPER = ".coral-Form-fieldwrapper";

    "use strict";
    function ourmUsers() {
        const wrapper = $(WRAPPER_CLASS),
            pathBrowser = wrapper.find("[data-init='pathbrowser']");

        wrapper.find("button[title='Browse']").hide();
        wrapper.find("input").first().css('width', 'inherit');
        wrapper.find('.coral-Form-fieldinfo').css('font-size', '12px');
        if (_.isEmpty(pathBrowser)) {
            return;
        }

        //set the search based pathbrowser loaders and renderers defined in author-search-based-pathbrowser.js
        pathBrowser.attr("data-autocomplete-callback", `${WORKDAY_PREFIX}.autocompletecallback`);
        pathBrowser.attr("data-option-loader", `${WORKDAY_PREFIX}.optionLoader`);
        pathBrowser.attr("data-option-renderer", `${WORKDAY_PREFIX}.optionRenderer`);
    };

    function populateOurmUserInputs() {
        const wrapper = $(this).parents(WRAPPER_CLASS);
        wrapper.find("input").first().val($(this).data('email'));
        wrapper.nextAll(FORM_FIELD_WRAPPER).find("[name='./username']").first().val($(this).data('username'));
        wrapper.nextAll(FORM_FIELD_WRAPPER).find("[name='./username']").first().removeAttr("disabled");
        wrapper.nextAll(FORM_FIELD_WRAPPER).find("[name='./username']").css({
            'background-color': '#eaeaea',
            'border-color': 'transparent',
            'color': '#b3b3b3',
            'cursor': 'default'
        });
    };

    $(document).on("foundation-contentloaded", ourmUsers);
    $(document).on('click', '.ourmUsers button', ourmUsers);
    $(document).on('click', '.ourmUserLi', populateOurmUserInputs);
})($, $(document));