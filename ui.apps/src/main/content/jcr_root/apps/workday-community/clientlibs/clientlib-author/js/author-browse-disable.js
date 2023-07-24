(function() {

function authorPageEdit() {
    const editableContainers = document.querySelectorAll('div.cq-Overlay--component[data-type="Editable"]');
    console.log('editableContainers22:', editableContainers);

    const observer = new MutationObserver(mutations => {
        mutations.forEach(mutation => {
            if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                const targetElement = mutation.target;
                if (targetElement.classList.contains('is-hover') &&
                    targetElement.classList.contains('is-selected') &&
                    targetElement.classList.contains('is-active')) {
                    console.log('The classes "is-hover is-selected is-active" were added to the container:', targetElement.textContent);
                    console.log('targetElement:', targetElement);

                    const configureButton = document.querySelector('button[data-action="CONFIGURE"]');
                    console.log('configureButton:::', configureButton);

                    if (configureButton) {
                        configureButton.addEventListener('click', () => {
                            console.log('after configure button click:::');
                            disableImageBrowse();

                            // Add another MutationObserver to detect the appearance of the specific element
                            /*const fileUploadObserver = new MutationObserver(fileUploadMutations => {
                                fileUploadMutations.forEach(fileUploadMutation => {
                                    if (fileUploadMutation.addedNodes.length > 0) {
                                        fileUploadMutation.addedNodes.forEach(node => {
                                            if (node.classList && node.classList.contains('coral-Link') && node.classList.contains('cq-FileUpload-browse')) {
                                                console.log("Element with class 'coral-Link cq-FileUpload-browse' has been added to the DOM:", node);
                                                // Change the style of the fileUploadElement here
                                                node.style.backgroundColor = 'red'; // For example, change the background color to red
                                                // You can modify other styles or add CSS classes as needed
                                                fileUploadObserver.disconnect(); // Stop observing once the element is found
                                            }
                                        });
                                    }
                                });
                            });

                            // Observe the document body for changes (you can observe a specific container if needed)
                            fileUploadObserver.observe(document.body, { childList: true, subtree: true });*/
                        });
                    }
                }
            }
        });
    });

    editableContainers.forEach(container => {
        observer.observe(container, { attributes: true });
    });

}

function disableImageBrowse() {
    console.log('inside disableImageBrowse method:::');

    const uploadOption = document.getElementsByClassName('cq-FileUpload-label');
    console.log('uploadOption::', uploadOption);

    const uploadOptionElement = document.querySelector('.cq-FileUpload-label');
        console.log('uploadOptionElement::', uploadOptionElement);

    const aTagWithinSpan = document.getElementsByClassName('coral-Link cq-FileUpload-browse');
    console.log('aTagWithinSpan::', aTagWithinSpan);

    console.log('222::');

    /*const aTagWithinSpan1 = document.querySelectorAll('.coral-Link.cq-FileUpload-browse');

    console.log('aTagWithinSpan1:::', aTagWithinSpan1);
    console.log('aTagWithinSpan1[0]:::', aTagWithinSpan1[0]);

    for (const aTag of aTagWithinSpan1) {
            console.log('Found <a> tag111:', aTag);
            aTag.style.pointerEvents = 'none'; // Re-enable pointer events
                aTag.style.color = 'gray';
            // Add your desired functionality or call a function here
        }

    for (const aTag of aTagWithinSpan) {
        console.log('Found <a> tag:', aTag.textContent);
        aTag.style.pointerEvents = 'none'; // Re-enable pointer events
            aTag.style.color = 'gray';
        // Add your desired functionality or call a function here
    }

    document.addEventListener('DOMContentLoaded', () => {
        const aTagWithinSpan = document.getElementsByClassName('coral-Link cq-FileUpload-browse');

        // Iterate through the HTMLCollection using for...of loop or traditional for loop
        for (const aTag of aTagWithinSpan) {
            console.log('Found <a> tag123:', aTag.textContent);
            aTag.style.pointerEvents = 'none'; // Re-enable pointer events
                        aTag.style.color = 'gray';
            // Add your desired functionality or call a function here
        }
    });*/

}

window.onload = authorPageEdit;
}());
