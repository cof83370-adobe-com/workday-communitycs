(function ($, $document) {
    "use strict";
    function ourmSpeakers() {
        var wrapper = $(".workday-search-pathbrowser-wrapper-speaker"),
                pathBrowser = wrapper.find("[data-init='pathbrowser']");
                wrapper.siblings(".coral-Form-fieldwrapper").hide();

                wrapper.find("button[title='Browse']").hide();
                const WORKDAY_PREFIX = "workday.granite.ui.search.pathBrowser";
                if(_.isEmpty(pathBrowser)){
                    console.log("WORKDAY - search path browser wrapper not found");
                    return;
                }

                //set the search based pathbrowser loaders and renderers defined in search-based-pathbrowser.js
                pathBrowser.attr("data-autocomplete-callback",`${WORKDAY_PREFIX}.autocompletecallback`);
                pathBrowser.attr("data-option-loader", `${WORKDAY_PREFIX}.optionLoader`);
                pathBrowser.attr("data-option-renderer", `${WORKDAY_PREFIX}.optionRenderer`);
                 $('.manualCheck input').each(function(e) {
                       populateInputsFromCheckbox(this);
                 });
       };

       function populateInputs() {
       var wrapper = $(this).parents(".workday-search-pathbrowser-wrapper-speaker");
       wrapper.find("input").first().val($(this).data('value'));
       wrapper.siblings(".coral-Form-fieldwrapper").find("input").first().val($(this).data('profile-image-data'));
       wrapper.siblings(".coral-Form-fieldwrapper").hide();
       };

       var populateInputsFromCheckbox  = function(ele) {
       console.log($(ele).prop('checked'));
       if($(ele).prop('checked')) {
           $(ele).parents('.manualCheck').siblings(".coral-Form-fieldwrapper").show();
       } else {
           $(ele).parents('.manualCheck').siblings(".coral-Form-fieldwrapper").hide();
       }
       };

$(document).on("foundation-contentloaded", ourmSpeakers );
$(document).on('click','.ourmSpeaker button', ourmSpeakers );

$(document).on('click','.speakerLi', populateInputs );
$(document).on('click','.manualCheck input', function(e){
  populateInputsFromCheckbox(this);
});
})($, $(document));