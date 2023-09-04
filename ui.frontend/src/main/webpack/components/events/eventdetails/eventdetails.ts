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
            localDateTimeString += '-1';
        } else if (initialDate < newDateObj.getDate()) {
            localDateTimeString += '+1';
        }

        return localDateTimeString;
    }

    function onDocumentReady() {

        const isEventsPage = document.getElementsByClassName('eventspage');
        if(isEventsPage) {
            const hiddenEventStartDate = document.getElementById('hiddenEventStartDate');
            if (hiddenEventStartDate) {
                const utcTimeString = hiddenEventStartDate.getAttribute('data-value');
                const hiddenUserTimeZone = document.getElementById('hiddenUserTimeZone');
                const timeZoneString = hiddenUserTimeZone.getAttribute('data-value') || Intl.DateTimeFormat().resolvedOptions().timeZone;

                if (utcTimeString) {
                    const localTime = convertUTCToLocal(utcTimeString, timeZoneString);
                    const eventDateElement = document.querySelector('.cmp-eventdetails__item-output') as HTMLElement;
                    eventDateElement.innerText = `${eventDateElement.innerText} (${localTime})`;
                }
            }
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());


