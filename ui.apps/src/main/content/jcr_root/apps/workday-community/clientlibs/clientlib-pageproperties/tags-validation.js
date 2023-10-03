(function(document, window, $, Granite) {
    /**
     * Max allowed tags on the page.
     * @type {number}
     */
    const MAX_ALLOWED_TAGS = 10;

    /**
     * Error message
     * @type {string}
     */
    const ERROR_MESSAGE = 'No more than 10 subscribable tags allowed on a page.';

    $(window).adaptTo('foundation-registry').register('foundation.validation.validator', {
        selector: '[data-foundation-validation=tag-limit]',
        validate: function(el) {
            let tagCount = 0;
            // Subscribable tags.
            const tags = ['./eventFormat', './industryTags', './productTags', './usingWorkdayTags', './regionCountryTags', './programsToolsTags', './releaseTags', './releaseNoteTags'];
            const invalidTags = [];
            $(tags).each(function (i, fieldName) {
                // Get all hidden input field with selected values.
                var tagElements = $('input[name="' + fieldName + '"]');
                if (tagElements && tagElements.length > 0) {
                    tagElements.each(function(index, tagElement){
                        if (tagElement.value && tagElement.value != "") {
                            tagCount++;
                            invalidTags.push(fieldName);
                        }
                    });
                }
            });

            $(tags).each(function (i, fieldName) {
                var tagElements = $('foundation-autocomplete[name="' + fieldName + '"]');
                if (tagElements  && tagElements.length > 0) {
                    tagElements.each(function(index, tagElement){
                        if (invalidTags.includes(fieldName) && tagCount > MAX_ALLOWED_TAGS) {
                            $(tagElement).setCustomValidity(ERROR_MESSAGE);
                            $(tagElement).updateErrorUI();
                            $(tagElement).setCustomValidity('');
                        }
                        else {
                            $(tagElement).setCustomValidity('');
                            $(tagElement).updateErrorUI();
                            if ($(tagElement).validationMessage() != '') {
                                $(tagElement).checkValidity();
                            }
                        }

                    });
                }
            });

            if (tagCount > MAX_ALLOWED_TAGS) {
                return ERROR_MESSAGE;
            }
        }
    });

})(document, window, Granite.$, Granite);

