const iframe = document.getElementById('ContentFrame');

function hideElements() {
    const doc = iframe.contentDocument;
    const elements = doc.body.querySelectorAll('.new.newpar.section');
    const filteredElements = Array.from(elements).filter(element => {
        return element.classList.length === 3;
    });

    filteredElements.forEach(function (filterElement, index) {
        filterElement.style.display = 'none';
    });
}

function ourmSpeakers() {
    var wrapper = $(".workday-search-pathbrowser-wrapper-speaker"),
            pathBrowser = wrapper.find("[data-init='pathbrowser']");
            pathBrowserButton = wrapper.find("button[title='Browse']").prop('disabled','disabled');
            const WORKDAY_PREFIX = "workday.granite.ui.search.pathBrowser";
            if(_.isEmpty(pathBrowser)){
                console.log("WORKDAY - search path browser wrapper not found");
                return;
            }

            //set the search based pathbrowser loaders and renderers defined in search-based-pathbrowser.js
            pathBrowser.attr("data-autocomplete-callback",`${WORKDAY_PREFIX}.autocompletecallback`);
            pathBrowser.attr("data-option-loader", `${WORKDAY_PREFIX}.optionLoader`);
            pathBrowser.attr("data-option-renderer", `${WORKDAY_PREFIX}.optionRenderer`);

   };

iframe.addEventListener('load', hideElements);
$(document).on("foundation-contentloaded", ourmSpeakers );
$(document).on('click','.ourmSpeaker button', ourmSpeakers );
