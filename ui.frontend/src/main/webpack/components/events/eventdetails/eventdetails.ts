(function() {

    function convertUTCToLocal(utcTimeString, targetTimezone) {
        const utcDatetime = new Date(utcTimeString);
        const targetOffsetMinutes = utcDatetime.getTimezoneOffset();
        const targetDatetime = new Date(utcDatetime.getTime() + targetOffsetMinutes * 60000);
        const isNextDay = utcDatetime.getDate() !== targetDatetime.getDate();

        const options: Intl.DateTimeFormatOptions = {
                            timeZone: targetTimezone,
                            hour12: true,
                            hour: 'numeric',
                            minute: 'numeric',
                            timeZoneName: 'short'
                        };

        let localTimeString = targetDatetime.toLocaleString('en-US', options);

        if (isNextDay) {
            localTimeString += ' +1';
        }

        return localTimeString;
    }

    function onDocumentReady() {
        const hiddenElement = document.getElementById('hiddenElement');
        const utcTimeString = hiddenElement.getAttribute('data-value');

        if(window.digitalData) {
            var targetTimezone = window.digitalData.user.timeZone;
        }

        if(utcTimeString && targetTimezone) {
            var localTime = convertUTCToLocal(utcTimeString, targetTimezone);
            const eventDateElement = document.querySelector('.cmp-eventdetails__item-output') as HTMLElement;
            eventDateElement.innerText = `${eventDateElement.innerText} (${localTime})`;
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());


