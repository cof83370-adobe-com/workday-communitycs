(function(document, window, $, Granite) {
    /**
     * Form id for page properties.
     * @type {string}
     */
    var PROPERTIES_FORM = "cq-sites-properties-form";
    /**
     * Max allowed tags on the page.
     * @type {number}
     */
    var MAX_ALLOWED_TAGS = 10;
    $(document).one("foundation-contentloaded", function(){
        // Click of save button.
        $("button[type='submit'][form='" + PROPERTIES_FORM + "']").on('click', function(e){
            var tagCount = 0;
            // Subscribable tags
            var tags = ["./eventFormat", "./industryTags", "./productTags",  "./usingWorkdayTags",  "./regionCountryTags",  "./programsToolsTags", "./releaseTags", "./releaseNoteTags"];
            $(tags).each(function (i, fieldName) {
                // Get all hidden input field with selected values.
                var tagElements = $('input[name="' + fieldName + '"]');
                if (tagElements != undefined && tagElements.length > 0) {
                    tagElements.each(function(index, tagElement){
                        if (tagElement.value != undefined && tagElement.value != "") {
                            tagCount++;
                        }
                    });
                }
            });
            if (tagCount > MAX_ALLOWED_TAGS) {
                alert("No more than 10 subscribable tags allowed on a page.");
                e.preventDefault();
                e.stopPropagation();
            }
        });
    });
})(document, window, Granite.$, Granite);