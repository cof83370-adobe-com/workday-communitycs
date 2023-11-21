
(function (document, channel, ns, $) {
  "use strict";
  const CONSTS = {
    TEXT_RESOURCETYPE: 'workday-community/components/core/text',
    DYNAMIC_TEXT_RESOURCETYPE: 'workday-community/components/dynamic/text',
    RTE_TOOLBAR: '.rte-toolbar.is-active',
    RTE_TABLE: 'button[title="Table"]',
    CQ_DIALOG_CONTENT: '.cq-dialog-content',
    CQ_DIALOG_ACTION: '.cq-dialog-actions',
    DIALOG_FULL_SCREEN: '.cq-dialog-layouttoggle'
  };
  // Hide the table option in dialog full screen mode
  $(document).on('dialog-ready', function () {
    const resourceType = ns.DialogFrame.currentDialog.editable.type;
    // Logic only executed on text and dynamic text component. 
    if (resourceType === CONSTS.TEXT_RESOURCETYPE || resourceType === CONSTS.DYNAMIC_TEXT_RESOURCETYPE) {
      $(CONSTS.CQ_DIALOG_ACTION).find(CONSTS.DIALOG_FULL_SCREEN).click(function () {
        console.log("[Dialog Ready] Option check for =" + ns.DialogFrame.currentDialog.editable.path);
        const rteToolbar = $(CONSTS.CQ_DIALOG_CONTENT).find(CONSTS.RTE_TOOLBAR);
        let tableOption = rteToolbar.find(CONSTS.RTE_TABLE);
        if (tableOption.length) {
          /**
           * AJAX GET call to to decide if the component option can be rendered to author
           * 
           */
          $.ajax({
            type: 'GET',
            url: '/bin/renderrtetable',
            success: function (response) {
              if ('renderTable' in response) {
                console.log("[renderTable] value is --> " + response.renderTable);
                if (!response.renderTable) {
                  tableOption[0].style.display = 'none';
                }
              }
            },
            error: function (jqXHR, textStatus, errorThrown) {
              console.error("Unable to determine on the RTE table option render");
            }
          });
        }
      });
    }

  });
  // Hide the table option in RTE full screen mode (inline)
  channel.on("cq-editor-loaded", function (event) {
    /**
     * AJAX GET call to to decide if the component option can be rendered to author
     * 
     */
    let editables = Granite.author.store;
    $.ajax({
      type: 'GET',
      url: '/bin/renderrtetable',
      success: function (response) {
        if ('renderTable' in response) {
          console.log("[renderTable] value is --> " + response.renderTable);
          $.each(editables, function (index, value) {
            if (editables[index].type === CONSTS.TEXT_RESOURCETYPE || editables[index].type === CONSTS.DYNAMIC_TEXT_RESOURCETYPE) {
              console.log("[CQ EDITOR LOADED] Option check for =" + editables[index].path);
              if (!response.renderTable) {
                editables[index].config.ipeConfig.rtePlugins.table.features = "-";
              }
            }
          });
        }
      },
      error: function (jqXHR, textStatus, errorThrown) {
        console.error("Unable to determine on the RTE table option render");
      }
    });
  });
})(document, jQuery(document), Granite.author, jQuery);