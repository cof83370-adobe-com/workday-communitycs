(function ($, $document) {
    const WORKDAY_PREFIX = "workday.granite.ui.search.pathBrowser",
        WRAPPER_CLASS = ".workday-search-pathbrowser-wrapper-ourmUser",
        MANUAL_OVERRIDE_CHECKBOX = ".manualOverride",
        FORM_FIELD_WRAPPER = ".coral-Form-fieldwrapper";

    "use strict";
    function ourmUsers() {
        const wrapper = $(WRAPPER_CLASS),
            pathBrowser = wrapper.find("[data-init='pathbrowser']");

        wrapper.nextAll(FORM_FIELD_WRAPPER).hide();
        wrapper.find("button[title='Browse']").hide();
        if (_.isEmpty(pathBrowser)) {
            return;
        }

        //set the search based pathbrowser loaders and renderers defined in search-based-pathbrowser.js
        pathBrowser.attr("data-autocomplete-callback", `${WORKDAY_PREFIX}.autocompletecallback`);
        pathBrowser.attr("data-option-loader", `${WORKDAY_PREFIX}.optionLoader`);
        pathBrowser.attr("data-option-renderer", `${WORKDAY_PREFIX}.optionRenderer`);

        $('.manualOverride input').each(function (e) {
            populateInputsFromCheckbox(this);
        });
    };

    function populateOurmUserInputs() {
        const wrapper = $(this).parents(WRAPPER_CLASS);
        wrapper.find("input").first().val($(this).data('value'));
        wrapper.siblings(FORM_FIELD_WRAPPER).find("input").first().val($(this).data('profile-image-data'));
        wrapper.nextAll(FORM_FIELD_WRAPPER).hide();
    };

    const populateInputsFromCheckbox = function (ele) {
        if ($(ele).prop('checked')) {
            $(ele).parents(FORM_FIELD_WRAPPER).siblings(FORM_FIELD_WRAPPER).show();
            $(ele).parents(FORM_FIELD_WRAPPER).siblings(FORM_FIELD_WRAPPER).find('input').val('');
        } else {
            $(ele).parents(FORM_FIELD_WRAPPER).siblings(FORM_FIELD_WRAPPER).hide();
        }
    };

    $(document).on("foundation-contentloaded", ourmUsers);
    $(document).on('click', '.ourmUsers button', ourmUsers);

    $(document).on('click', '.ourmUserLi', populateOurmUserInputs);
    $(document).on('click', '.manualOverride input', function (e) {
        populateInputsFromCheckbox(this);
    });
})($, $(document));