(function() {

    function convertUTCToLocal(utcTimeString, targetTimezone) {

        const newDateObj = new Date(utcTimeString);

        const options: Intl.DateTimeFormatOptions = {
          timeZone: targetTimezone,
          hour12: true,
          hour: '2-digit',
          minute: '2-digit',
          timeZoneName: 'short'
        };

        const initialDate = newDateObj.getUTCDate();

        let localDateTimeString = newDateObj.toLocaleString('en-US', options);

        if (initialDate > newDateObj.getDate()) {
            localDateTimeString = newDateObj.toLocaleString('en-US', options) + ' - 1';
        } else if (initialDate < newDateObj.getDate()) {
            localDateTimeString = newDateObj.toLocaleString('en-US', options) + ' + 1';
        }

        return localDateTimeString;
    }

    function onDocumentReady() {

        const isEventsPage = document.getElementsByClassName('eventspage');
        if(isEventsPage) {
            const hiddenElement = document.getElementById('hiddenElement');
            const utcTimeString = hiddenElement.getAttribute('data-value');

            if(utcTimeString) {
                if ('digitalData' in window) {
                    const targetTimezone = (window as CustomWindow).digitalData.user?.timeZone || Intl.DateTimeFormat().resolvedOptions().timeZone;
                    var localTime = convertUTCToLocal(utcTimeString, targetTimezone);
                    const eventDateElement = document.querySelector('.cmp-eventdetails__item-output') as HTMLElement;
                    eventDateElement.innerText = `${eventDateElement.innerText} (${localTime})`;
                }
            }
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());


