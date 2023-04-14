
(function (document, $, ns) {
  "use strict";

$(document).on('dialog-ready', function () {
  const BOOK_COMP_CLASS = '.book-comp-class';
  const CQ_DIALOG_SUBMIT_CLASS = '.cq-dialog-submit';
  // Only do custom logic if we're using our book component. The below id
  if ($(BOOK_COMP_CLASS).length > 0) {
    // on submit of the book component that will be registered for click event.
    $(BOOK_COMP_CLASS).parents().find(CQ_DIALOG_SUBMIT_CLASS).click(function () {
      //Get the book dialog form
      const $form = $(this).closest('form.foundation-form'); //get jquery variable based on 'richtext'
      const MODULE_NAME = 'Book Operations';

      function createBookData($bookForm) {
        const formData = $bookForm.serializeArray();
        const filteredFormData = formData.filter(j => j.value != '' && j.name.includes('path')).map(o => o.value)

        return filteredFormData;
      }

      /**
       * AJAX POST call to send and persist the book tool meta data found on this page for server-side HTML processing
       * @param {string} url Generally the data-path (content-path) of the Book component plus some AEM url selectors appended.
       * @param {BookPathData[]} discData The book path data to send to the server for processing.
       * @param {string} bookResPath The book Resource Path.
       */
      function sendBookData(url, bookPathData, bookResPath) {
        // Use Adobe's Foundation UI framework for better prompt UI
        const FOUNDATION_UI = $(window).adaptTo('foundation-ui');
        const NOTIFY_TITLE = 'Book Processing';

        /* NOTE: We're forced to use Adobe's cq.jquery for POST requests
        so that a CSRF token can be passed to the server.
        We'll get HTTP 403 with any other method */
        $.ajax({
          type: 'POST',
          url: url,
          data: {
            'bookResPath': bookResPath,
            'bookPathData': JSON.stringify(bookPathData)
          },
          dataType: 'json',
          success: function (response) {
            if ('success' in response && response.success === true) {
              console.log(MODULE_NAME, response);
              // Create a Foundation UI Prompt, display feedback to the user, and refresh the page when the user clicks 'Ok'
              FOUNDATION_UI.prompt(NOTIFY_TITLE, `Please activate listed books to make the updates complete ${response.pagePaths}.`, 'success', [{
                id: 'accept',
                text: 'Ok'
              }]);
            } else {
              FOUNDATION_UI.alert(NOTIFY_TITLE, 'Failed!', 'error');
              console.error(MODULE_NAME, 'AJAX success block but something went wrong on the server: ', response);
            }
          },
          error: function (jqXHR, textStatus, errorThrown) {
            console.error(MODULE_NAME, jqXHR, textStatus, errorThrown);
            FOUNDATION_UI.alert(NOTIFY_TITLE, 'Failed!', 'error');
          }
        });
      }

      /**
       * Handle the click of the Book component submit button by creating the book path array data structure
       */
      function handleProcessClick($bookForm) {
        try {
          const BOOK_DATA = createBookData($bookForm);
          const API_URL = '/bin/processBookPages';
          const BOOK_COMP_PATH = ns.DialogFrame.currentDialog.editable.config.path;
          console.log(API_URL);
          sendBookData(API_URL, BOOK_DATA, BOOK_COMP_PATH);
        } catch (error) {
          console.error(MODULE_NAME, error);
        }
      }

      //The click event has been trigged and we will execute the handleProcessClick that will proceed to create and send the book path data
      handleProcessClick($form);

      $form.submit();
    });
  }
});
//@ts-ignore
})(document, Granite.$, Granite.author);