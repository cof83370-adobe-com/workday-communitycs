(function ($) {
    "use strict";

    $(document).on('dialog-ready', function () {
        function reset($event, elm) {
            if (elm.validate()) {
                $event.preventDefault();
                $event.stopPropagation();
                elm.parent().validate().reset();
            }
        }

        const elm = $("coral-dialog coral-panel-content coral-select");
        if (elm.data("vmin")) {
            elm.addEventListener("change", function ($event) {
                reset($event, elm);
            });

            const button = $("coral-dialog coral-panel-content coral-select button");
            if (button) {
                button.addEventListener("click", function($event) {
                    reset($event, button.parent());
                })
            }
        }
    })

    $.validator.register("foundation.validation.validator", {
        selector: "coral-select",
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