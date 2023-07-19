(function() {

    function convertUTCToLocal(utcTimeString, targetTimezone) {

        const originalDateObj = new Date(utcTimeString);

        const options: Intl.DateTimeFormatOptions = {
          timeZone: targetTimezone,
          hour12: true,
          hour: '2-digit',
          minute: '2-digit',
          timeZoneName: 'short'
        };

        const convertedDateObj = new Date(originalDateObj.toLocaleString('en-US'));

        let localDateTimeString = originalDateObj.toLocaleString('en-US', options);

        if (originalDateObj.getDate() > convertedDateObj.getDate()) {
            convertedDateObj.setDate(convertedDateObj.getDate() + 1);
            localDateTimeString = convertedDateObj.toLocaleString('en-US', options) + ' - 1';
        } else if (originalDateObj.getDate() < convertedDateObj.getDate()) {
            convertedDateObj.setDate(convertedDateObj.getDate() - 1);
            localDateTimeString = convertedDateObj.toLocaleString('en-US', options) + ' + 1';
        }

        return localDateTimeString;
    }

    function onDocumentReady() {

        const isEventsPage = document.getElementsByClassName('eventspage');
        if(isEventsPage) {
            const hiddenElement = document.getElementById('hiddenElement');
            const utcTimeString = hiddenElement.getAttribute('data-value');

            if(utcTimeString) {
                if(window.digitalData) {
                    var targetTimezone = window.digitalData.user.timeZone;
                }

                if(utcTimeString && targetTimezone) {
                    var localTime = convertUTCToLocal(utcTimeString, targetTimezone);
                    const eventDateElement = document.querySelector('.cmp-eventdetails__item-output') as HTMLElement;
                    eventDateElement.innerText = `${eventDateElement.innerText} (${localTime})`;
                }
            }
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());


