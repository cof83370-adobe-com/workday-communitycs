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
                $('.coral-label-display').hide();
            } else {
                $('.coral-label-display').show();
            }
        });
	});
})(document, Granite.$, Coral);