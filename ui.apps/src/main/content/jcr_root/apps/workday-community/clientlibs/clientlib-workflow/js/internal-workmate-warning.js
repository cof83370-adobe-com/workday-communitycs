(function (document, $, Coral) {
	var $doc = $(document);
    
	$doc.on('foundation-contentloaded', function(e) {
        var payloadpath = $('.external-dialog-injection').data('payloadpath');
        var origin = window.location.origin;
		var url = origin+payloadpath+"/jcr:content.json";
        var propVal = "";

        $.getJSON( url, function( json ) {
			propVal = json.accessControlTags;
            if(propVal.includes("access-control:internal_workmates")) {
                $('.internal-workmate-warning-label').hide();
            } else {
                $('.internal-workmate-warning-label').show();
            }
        });
	});
})(document, Granite.$, Coral);