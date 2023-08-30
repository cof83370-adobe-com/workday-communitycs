(function () {
    function showFeedbackContainer() {
        const no = document.getElementById('feedbackNoButtonId');
        const yes = document.getElementById('feedbackYesButtonId');
        const feedbackContainer = document.getElementById('feedback-form-Container');
        const dropdowContainer = document.getElementById('feedback-dropdownContainer');
        const successImage = document.getElementById('feedback-successContainer');
        no.classList.toggle('clicked');
        if (yes.classList.contains('clicked')) {
            yes.classList.remove('clicked');
        }
        if (feedbackContainer.style.display === 'none') {
            feedbackContainer.style.display = 'block';
            successImage.style.display = 'none';
        } else {
            feedbackContainer.style.display = 'none';
            dropdowContainer.style.border = '1px solid #78858F';
            successImage.style.display = 'none';
        }
    }

    function showSuccessImage() {
        const yes = document.getElementById('feedbackYesButtonId');
        const no = document.getElementById('feedbackNoButtonId');
        const feedbackContainer = document.getElementById('feedback-form-Container');
        const successImage = document.getElementById('feedback-successContainer');
        successImage.style.display = 'flex';
        feedbackContainer.style.display = 'none';
        yes.classList.add('feeback-button-disable');
        no.classList.add('feeback-button-disable');
        no.classList.remove('clicked');
    }

    function toggleDropdown() {
        const dropdownToggle = document.querySelector('.feedback-option-dropdown-toggle');
        const dropdownMenu = document.getElementById('feedback-component-dropdownMenu');
        const dropdowContainer = document.getElementById('feedback-dropdownContainer');
        dropdownToggle.classList.toggle('clicked');
        dropdownMenu.classList.toggle('open');
        dropdowContainer.style.border = '2px solid #0875E1';
    }

    function cancelFeedback() {
        const no = document.getElementById('feedbackNoButtonId');
        const feedbackContainer = document.getElementById('feedback-form-Container');
        feedbackContainer.style.display = 'none';
        no.classList.remove('clicked');
    }

    function selectOption(option) {
        const selectedOption = document.querySelector('.feedback-dropdown-selected-option');
        selectedOption.textContent = option;
        toggleDropdown();
    }

    function toggleButtonColor(button) {
        button.classList.toggle('clicked');
    }

    const feedback = document.getElementById('feedback-component-container');
    if (feedback) {
        document.addEventListener('click', (event: MouseEvent) => {
            const dropdown = document.querySelector('.feedback-option-dropdown-toggle') as HTMLElement;
            const dropdownButton = document.getElementById('feedback-component-dropdownMenu') as HTMLElement;
            if (!dropdown.contains(event.target as Node) && event.target !== dropdownButton) {
                dropdownButton.classList.remove('open');
            }
        });

        const yesButton = document.getElementById('feedbackYesButtonId');
        const noButton = document.getElementById('feedbackNoButtonId');
        const cancelButton = document.getElementById('feedback-form-cancel-button');
        const submitButton = document.querySelector('.feedback-form-submit-button button');
        const dropdownToggle = document.querySelector('.feedback-option-dropdown-toggle');
        yesButton.addEventListener('click', showSuccessImage);
        noButton.addEventListener('click', showFeedbackContainer);
        dropdownToggle.addEventListener('click', toggleDropdown);
        cancelButton.addEventListener('click', cancelFeedback);

        const dropdownOptions = document.querySelectorAll('.feedback-dropdown-option');
        dropdownOptions.forEach(function (option) {
            option.addEventListener('click', function () {
                const optionText = option.textContent;
                selectOption(optionText);
            });
        });
    }
})();