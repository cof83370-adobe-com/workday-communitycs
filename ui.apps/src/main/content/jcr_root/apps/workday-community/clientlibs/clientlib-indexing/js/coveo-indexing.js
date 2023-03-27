$(document).ready(function(){ 
  $("#coveoIndexAllButton").click(function() {
    $("#indexError").hide();
    $("#indexError").html("");
    let templateListString = $("#coveoIndexTemplateList").attr('data-pages');
    let templateList = templateListString.split(',');
    if (templateList.length) {
      $.post("/bin/coveo/index-all", {templates: templateList}, function (result) {
        console.log(result);
        $("#indexError").show();
        $("#indexError").html(result);
      });
      return false;
    }
  });

  $( "#coveoDeleteAllButton" ).click(function() {
    return false;
  });
});