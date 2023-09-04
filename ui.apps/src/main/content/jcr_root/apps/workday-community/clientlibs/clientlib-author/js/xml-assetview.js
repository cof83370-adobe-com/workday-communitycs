(function ($, $document, author) {
    var self = {},
        CONTENT_FINDER_NAME = 'XML Files';
    self.viewInAdminRoot = "/assetdetails.html{+item}";
    
    populateXMLAssets();
    
    function populateXMLAssets() {

        var xmlAssetServlet = '/bin/wcm/contentfinder/asset/view.html',
            itemResourceType = 'cq/gui/components/authoring/assetfinder/asset';

        self.loadAssets = function (query, lowerLimit, upperLimit) {
            
            var param = {
                '_dc': new Date().getTime(),
                'query': query.concat("order:\"-jcr:content/jcr:lastModified\" "),
                'mimeType': 'application/xml',
                'itemResourceType': itemResourceType,
                'limit': lowerLimit + ".." + upperLimit,
                '_charset_': 'utf-8'
            };

            return $.ajax({
                type: 'GET',
                dataType: 'html',
                url: Granite.HTTP.externalize(xmlAssetServlet),
                data: param
            });
        };
    }
    author.ui.assetFinder.register(CONTENT_FINDER_NAME, self);
}(jQuery, jQuery(document), Granite.author));