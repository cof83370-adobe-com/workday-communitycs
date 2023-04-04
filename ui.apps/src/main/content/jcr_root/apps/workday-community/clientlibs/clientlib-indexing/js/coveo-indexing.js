$(document).ready(function(){
  let errorDiv = $('#indexError');
  $('#coveoIndexAllButton').click(function() {
    errorDiv.html('');
    errorDiv.hide();
    let templateListString = $('#coveoIndexTemplateList').attr('data-pages');
    let templateList = templateListString.split(',');
    if (templateList.length) {
      $.post('/bin/coveo/index-all', {templates: templateList}, function (result) {
        errorDiv.show();
        errorDiv.html(result);
      });
      return false;
    }
  });

  $('#coveoDeleteAllButton').click(function() {
    errorDiv.html('');
    errorDiv.hide();
    if(confirm('Are you sure you want to delete all contents from Coveo.?')){
      $.ajax({
        url: '/bin/coveo/delete-all',
        type: 'DELETE',
        success: function (result) {
          errorDiv.show();
          errorDiv.html(result);
        },
        error: function (result) {
          errorDiv.show();
          errorDiv.html('Something went wrong.');
        }
      });
    }
    return false;
  });
});