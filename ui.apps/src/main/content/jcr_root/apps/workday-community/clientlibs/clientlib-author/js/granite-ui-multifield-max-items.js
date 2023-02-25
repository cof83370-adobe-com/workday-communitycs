(function ($, Coral) {
    'use strict';

    /**
     * Granite UI Validation - Multifield Max Items
     *
     * @class MultifieldMaxItems
     * @classdesc registering Granite UI validation for the multifield maximum items.
     * This custom JavaScript validation ensures the number of multifield items does not exceed the maximum number
     * of items in the touch ui.
     *
     * Example Granite UI Multifield block:
     * <navigationLinksMultifield
          jcr:primaryType="nt:unstructured"
          granite:class="granite-ui-validation-multifield-max-items"
          validation="max-items-2"
          sling:resourceType="granite/ui/components/coral/foundation/form/multifield"
          ...
     */
    var MultifieldMaxItems = function () {

        var CONST = {
            TARGET_GRANITE_UI: '.granite-ui-validation-multifield-max-items',
            VALIDATION_PROPERTY: 'validation',
            VALIDATION_VALUE_PREFIX: 'max-items',
            ERROR_MESSAGE: 'Maximum allowed items are {0}',
            ERROR_TOOLTIP_ID: Coral.commons.getUID(),
            DEFAULT_MAX_SIZE:  100
        };

        /**
         * Initializes the MultifieldMaxItems
         *
         * @private
         */
        function init() {

            // if dialog does not exist on the page, escape.
            if (!$(CONST.TARGET_GRANITE_UI)) return;

            // register foundation validation for multi-field, max size
            $(window).adaptTo('foundation-registry').register('foundation.validation.validator', {
                selector: CONST.TARGET_GRANITE_UI,
                validate: function (el) {
                    var max = _getMaxSizeValue(el);
                    if (el.items.length > max) {
                        return CONST.ERROR_MESSAGE.replace('{0}', max);
                    }
                },
                show: function (el, message) {
                    _handleError(el, message);
                },
                clear: function (el) {
                    var max = _getMaxSizeValue(el);
                    if (el.items.length <= max) {
                        // set field to valid if max size fits within the rules
                        var $el = $(el);
                        var fieldAPI = $el.adaptTo('foundation-field');
                        if (fieldAPI && fieldAPI.setInvalid) {
                            fieldAPI.setInvalid(false);
                            // remove tooltip
                            $('#' + CONST.ERROR_TOOLTIP_ID).remove();
                        }
                    }
                }
            });
        }

        /**
         * Gets the max-items value set in the "validation" granite ui property.
         *
         * @private
         * @param {HTMLElement} el is the validation HTML block with an existing validation attribute
         */
        function _getMaxSizeValue(el) {
            var validationName = el.getAttribute(`data-${CONST.VALIDATION_PROPERTY}`);
            if (validationName) {
                var max = validationName.replace(`${CONST.VALIDATION_VALUE_PREFIX}-`, '');
                if (max) {
                    return parseInt(max);
                }
            }
            return CONST.DEFAULT_MAX_SIZE;
        }


        /**
         * Handles the displaying of error messages
         *
         * @private
         * @param {HTMLElement} el is the validation HTML block with an existing validation attribute
         * @param {String} message is error message returned by the validate method, in the registry of foundation.validation.validator.
         */
        function _handleError(el, message) {
            var $el = $(el);

            // set foundation field to show as invalid
            var fieldAPI = $el.adaptTo('foundation-field');
            if (fieldAPI && fieldAPI.setInvalid) {
                fieldAPI.setInvalid(true);
            }

            // set foundation field to show as invalid
            var error = $el.data('foundation-validation.internal.error');
            if (error && !error.parentNode) {
                error.content.innerHTML = message;
                    $el.after(error);
                    error.show();
            } else {
                error = new Coral.Tooltip();
                error.variant = 'error';
                error.interaction = 'off';
                error.placement = 'bottom';
                error.target = el;
                error.content.innerHTML = message;
                error.open = true;
                error.id = CONST.ERROR_TOOLTIP_ID;
                $el.data('foundation-validation.internal.error', error);
                $el.after(error);
            }
        }

        return {
            init: init
        }

    }();

    MultifieldMaxItems.init();

}(jQuery, Coral));