(function() {
    const download = 'cmp-download';
    var downloadSelectors = {
        item: `[class="${download}"]`,
        titleLink: `[class="${download}__title-link"]`
    };

    function getFileExtension(config: any) {

        const hasTitle = config.element.querySelector(downloadSelectors.titleLink);

        if(hasTitle){
            const downloadTitleLink = config.element.querySelector(downloadSelectors.titleLink);
            const fileUrl = downloadTitleLink.href;
            const str = 'coredownload';
            const fileExtension = fileUrl.substr(fileUrl.indexOf(str) + str.length);
            downloadTitleLink.append(fileExtension);
        } else {
            const downloadImg = new Image();
            downloadImg.src = '/etc.clientlibs/community/clientlibs/clientlib-site/resources/images/download.svg';
            config.element.append(downloadImg);
            downloadImg.className = `${download}__img`;
        }
    }

    function onDocumentReady() {
        const elements = document.querySelectorAll(downloadSelectors.item);
        for (var i = 0; i < elements.length && elements[i]; i++) {
            getFileExtension({ element: elements[i] });
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());