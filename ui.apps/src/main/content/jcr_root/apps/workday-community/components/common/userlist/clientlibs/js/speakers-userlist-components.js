(function ($, $document) {
    const WORKDAY_PREFIX = "workday.granite.ui.search.pathBrowser",
        WRAPPER_CLASS = ".workday-search-pathbrowser-wrapper-speaker",
        MANUAL_OVERRIDE_CHECKBOX = ".manualOverride",
        FORM_FIELD_WRAPPER = ".coral-Form-fieldwrapper";

    "use strict";
    function ourmSpeakers() {
        const wrapper = $(WRAPPER_CLASS),
            pathBrowser = wrapper.find("[data-init='pathbrowser']");

        wrapper.siblings(FORM_FIELD_WRAPPER).hide();
        wrapper.find("button[title='Browse']").hide();
        if (_.isEmpty(pathBrowser)) {
            console.log("WORKDAY - search path browser wrapper not found");
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

    function populateSpeakersInputs() {
        const wrapper = $(this).parents(WRAPPER_CLASS);
        wrapper.find("input").first().val($(this).data('value'));
        wrapper.siblings(FORM_FIELD_WRAPPER).find("input").first().val($(this).data('profile-image-data'));
        wrapper.siblings(FORM_FIELD_WRAPPER).hide();
    };

    const populateInputsFromCheckbox = function (ele) {
        if ($(ele).prop('checked')) {
            $(ele).parents(MANUAL_OVERRIDE_CHECKBOX).siblings(FORM_FIELD_WRAPPER).show();
        } else {
            $(ele).parents(MANUAL_OVERRIDE_CHECKBOX).siblings(FORM_FIELD_WRAPPER).hide();
        }
    };

    $(document).on("foundation-contentloaded", ourmSpeakers);
    $(document).on('click', '.ourmSpeakers button', ourmSpeakers);

    $(document).on('click', '.speakerLi', populateSpeakersInputs);
    $(document).on('click', '.manualOverride input', function (e) {
        populateInputsFromCheckbox(this);
    });
})($, $(document));