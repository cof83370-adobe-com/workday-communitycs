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
            const hiddenEventEndDate = document.getElementById('hiddenEventEndDate');
            const wcmmode = document.getElementById('publishmode');
            if (hiddenEventStartDate) {
                const utcTimeString = hiddenEventStartDate.getAttribute('data-value');
                const hiddenUserTimeZone = document.getElementById('hiddenUserTimeZone');
                const timeZoneString = hiddenUserTimeZone.getAttribute('data-value') || Intl.DateTimeFormat().resolvedOptions().timeZone;

                if (utcTimeString) {
                    const localTime = convertUTCToLocal(utcTimeString, timeZoneString);
                    const eventDateElement = document.querySelector('.cmp-eventdetails__item-output') as HTMLElement;
                    eventDateElement.innerText = `${eventDateElement.innerText} (${localTime})`;
                }

                if (hiddenEventEndDate) {
                    const registerEventButton = document.querySelector('.event-registration .cmp-button') as HTMLElement;
                    const authoredEventEndDate = hiddenEventEndDate.getAttribute('data-value');
                    const EventEndDate = new Date(authoredEventEndDate);
                    const now = new Date();
                    if (now > EventEndDate && wcmmode != null) {
                        registerEventButton.style.display = 'none';
                    }
                    else {
                        registerEventButton.style.display = 'flex';
                    }
                }
            }
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());


