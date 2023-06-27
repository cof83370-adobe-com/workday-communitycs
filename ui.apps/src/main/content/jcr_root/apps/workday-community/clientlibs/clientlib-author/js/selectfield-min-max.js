(function ($) {
    "use strict";
    $.validator.register("foundation.validation.validator", {
        selector: "[data-validation=event-feeds]",
        validate: function (el) {
            if ($(el).data("vmin")) {
                const min = $(el).data("vmin");
                const totalSelections = $(el).children("coral-taglist").children().size();
                if (totalSelections < min) {
                    return "Minimum numbers of items required are: " + min;
                }
            }
            if ($(el).data("vmax")) {
                const max = $(el).data("vmax");
                const totalSelections = $(el).children("coral-taglist").children().size();
                if (totalSelections > max) {
                    return "Maximum numbers of items allowed are: " + max;
                }
            }
        }
    });
})($);