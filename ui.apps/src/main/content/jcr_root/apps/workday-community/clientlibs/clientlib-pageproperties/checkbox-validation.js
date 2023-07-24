(function(document, window, $, Granite) {
    "use strict";

    var $RECURRING_EVENT_CHECKBOX_SELECTOR = ".recurring-events-checkbox";
    var $RECURRING_EVENT_RADIO_GROUP_SELECTOR = ".recurring-events-radio-group";

    function hide() {
        $($RECURRING_EVENT_RADIO_GROUP_SELECTOR).children().each(function() {
            var element = $(this).prop("disabled", true);
        });
    }

    function show() {
        $($RECURRING_EVENT_RADIO_GROUP_SELECTOR).children().each(function() {
            var element = $(this).prop("disabled", false);
        });
    }

    // When the dialog is loaded, init all slaves
    $(document).on("foundation-contentloaded", function() {
        var reqOperation = ($($RECURRING_EVENT_CHECKBOX_SELECTOR ).prop('checked') == true) ? show() : hide();
    });


    // When a value is changed, trigger update
    $(document).on("change", function(e) {
       var reqOperation = ($($RECURRING_EVENT_CHECKBOX_SELECTOR ).prop('checked') == true) ? show() : hide();
    });

})(document, window, Granite.$, Granite);