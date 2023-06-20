(function(document, window, $, Granite) {
    "use strict";

        var validateHandler = function(e) {
            var $formFields = $(document).find(".foundation-form .coral-Form-field[required], .foundation-form .coral-Form-field[aria-required='true']");

            $.each($formFields, function(index, element) {
                const api = $(this).adaptTo("foundation-validation");
                 if (api && !api.checkValidity()){
                    $(this).adaptTo("foundation-field").setInvalid(true);
                 }
            });
        };

        $(document).on("foundation-contentloaded", function () {
              setTimeout(validateHandler, 200);
        });

})(document, window, Granite.$, Granite);