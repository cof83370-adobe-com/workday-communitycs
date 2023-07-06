(function() {
    function showFeedbackContainer() {
        var no = document.getElementById('feedbackNoButtonId');
        var yes = document.getElementById('feedbackYesButtonId');
        var feedbackContainer = document.getElementById('feedbackContainer');
        var container = document.getElementById('container');
        var buttonContainer = document.getElementById('buttonContainer');
        var dropdowContainer = document.getElementById('dropdowContainer');
        var successImage = document.getElementById('successContainer');
        no.classList.toggle('clicked');
        if (yes.classList.contains('clicked')) {
            yes.classList.remove('clicked');
        }
        if (feedbackContainer.style.display === 'none') {
            feedbackContainer.style.display = 'block';
            container.style.height = '337px';
            buttonContainer.style.marginTop = '17px';
            buttonContainer.style.marginLeft = '-8px';
            buttonContainer.style.height = '40px';
            successImage.style.display = 'none';
        } else {
            feedbackContainer.style.display = 'none';
            container.style.height = '80px';
            buttonContainer.style.marginTop = '0px';
            buttonContainer.style.height = '80px';
            dropdowContainer.style.border = '1px solid #78858F';
            successImage.style.display = 'none';
        }
    }

    function showSuccessImage() {
        var yes = document.getElementById('feedbackYesButtonId');
        var no = document.getElementById('feedbackNoButtonId');
        var feedbackContainer = document.getElementById('feedbackContainer');
        var container = document.getElementById('container');
        var buttonContainer = document.getElementById('buttonContainer');
        var successImage = document.getElementById('successContainer');
        yes.classList.toggle('clicked');
        if (no.classList.contains('clicked')) {
            no.classList.remove('clicked');
        }
        if (successImage.style.display === 'none') {
            successImage.style.display = 'grid';
            container.style.height = '337px';
            buttonContainer.style.marginTop = '17px';
            buttonContainer.style.height = '40px';
            feedbackContainer.style.display = 'none';
        } else {
            feedbackContainer.style.display = 'none';
            container.style.height = '80px';
            buttonContainer.style.marginTop = '0px';
            buttonContainer.style.height = '80px';
            successImage.style.display = 'none';
        }
    }

    function toggleDropdown() {
        var dropdownToggle = document.querySelector('.dropdown-toggle');
        var dropdownMenu = document.getElementById('dropdownMenu');
        var dropdowContainer = document.getElementById('dropdowContainer');
        dropdownToggle.classList.toggle('clicked');
        dropdownMenu.classList.toggle('open');
        dropdowContainer.style.border = '2px solid #0875E1';
    }

    function selectOption(option) {
        var selectedOption = document.querySelector('.selected-option');
        selectedOption.textContent = option;
        toggleDropdown();
    }

    function toggleButtonColor(button) {
        button.classList.toggle('clicked');
    }
    var yesButton = document.getElementById('feedbackYesButtonId');
    var noButton = document.getElementById('feedbackNoButtonId');
    var submitButton = document.querySelector('.feedback-form-submit-button button');
    var dropdownToggle = document.querySelector('.dropdown-toggle');
    yesButton.addEventListener('click', showSuccessImage);
    noButton.addEventListener('click', showFeedbackContainer);
    dropdownToggle.addEventListener('click', toggleDropdown);

    var dropdownOptions = document.querySelectorAll('.dropdown-option');
    dropdownOptions.forEach(function(option) {
        option.addEventListener('click', function() {
            var optionText = option.textContent;
            selectOption(optionText);
        });
    });
})();