
(function (window, channel, ns, $) {
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
  // Hide the table option in RTE full screen mode (inline)
  channel.on("cq-editor-loaded", function (event) {
    /**
     * AJAX GET call to to decide if the component option can be rendered to author
     * 
     */
    window.WorkdayAemAuthorship = {};
    WorkdayAemAuthorship.renderTable = false;
    $.ajax({
      type: 'GET',
      url: '/bin/workday/community/authorship',
      success: function (response) {
        if ('render' in response) {
          WorkdayAemAuthorship.renderTable = response.render;
          if (!WorkdayAemAuthorship.renderTable) {
            hideTableOption()
          }
        }
      },
      error: function (jqXHR, textStatus, errorThrown) {
        console.error("Unable to determine on the RTE table option render");
        hideTableOption();
      }
    });
    function hideTableOption() {
      let editables = Granite.author.store;
      $.each(editables, function (index, value) {
        if (editables[index].type === CONSTS.TEXT_RESOURCETYPE || editables[index].type === CONSTS.DYNAMIC_TEXT_RESOURCETYPE) {
          editables[index].config.ipeConfig.rtePlugins.table.features = "-";
        }
      });
    }
  });
  // Hide the table option in dialog full screen mode
  channel.on('dialog-ready', function () {
    const resourceType = ns.DialogFrame.currentDialog.editable.type;
    // Logic only executed on text and dynamic text component. 
    if (resourceType === CONSTS.TEXT_RESOURCETYPE || resourceType === CONSTS.DYNAMIC_TEXT_RESOURCETYPE) {
      $(CONSTS.CQ_DIALOG_ACTION).find(CONSTS.DIALOG_FULL_SCREEN).click(function () {
        const rteToolbar = $(CONSTS.CQ_DIALOG_CONTENT).find(CONSTS.RTE_TOOLBAR);
        let tableOption = rteToolbar.find(CONSTS.RTE_TABLE);
        if (tableOption.length && !WorkdayAemAuthorship.renderTable) {
          tableOption[0].style.display = 'none';
        }
      });
    }

  });
})(this, jQuery(document), Granite.author, jQuery);