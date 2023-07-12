(function() {
    function showFeedbackContainer() {

        var no = document.getElementById('feedbackNoButtonId');
        var yes = document.getElementById('feedbackYesButtonId');
        var feedbackContainer = document.getElementById('feedback-form-Container');
        var dropdowContainer = document.getElementById('feedback-dropdownContainer');
        var successImage = document.getElementById('feedback-successContainer');
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
        var yes = document.getElementById('feedbackYesButtonId');
        var no = document.getElementById('feedbackNoButtonId');
        var feedbackContainer = document.getElementById('feedback-form-Container');
        var successImage = document.getElementById('feedback-successContainer');
        yes.classList.toggle('clicked');
        if (no.classList.contains('clicked')) {
            no.classList.remove('clicked');
        }
        if (successImage.style.display === 'none') {
            successImage.style.display = 'grid';
            feedbackContainer.style.display = 'none';
        } else {
            feedbackContainer.style.display = 'none';
            successImage.style.display = 'none';
        }
    }

    function toggleDropdown() {
        var dropdownToggle = document.querySelector('.feedback-option-dropdown-toggle');
        var dropdownMenu = document.getElementById('feedback-component-dropdownMenu');
        var dropdowContainer = document.getElementById('feedback-dropdownContainer');
        dropdownToggle.classList.toggle('clicked');
        dropdownMenu.classList.toggle('open');
        dropdowContainer.style.border = '2px solid #0875E1';
    }

    function selectOption(option) {
        var selectedOption = document.querySelector('.feedback-dropdown-selected-option');
        selectedOption.textContent = option;
        toggleDropdown();
    }

    function toggleButtonColor(button) {
        button.classList.toggle('clicked');
    }
	

  

  



	
	
    var yesButton = document.getElementById('feedbackYesButtonId');
    var noButton = document.getElementById('feedbackNoButtonId');
    var submitButton = document.querySelector('.feedback-form-submit-button button');
    var dropdownToggle = document.querySelector('.feedback-option-dropdown-toggle');
    yesButton.addEventListener('click', showSuccessImage);
    noButton.addEventListener('click', showFeedbackContainer);
    dropdownToggle.addEventListener('click', toggleDropdown);

    var dropdownOptions = document.querySelectorAll('.feedback-dropdown-option');
    dropdownOptions.forEach(function(option) {
        option.addEventListener('click', function() {
            var optionText = option.textContent;
            selectOption(optionText);
        });
    });
})();