(function() {
    const video = 'video';
    const videoSelectors = {
        playOption: '[class="play-video"]'
    };
    const videoPlayer = document.getElementsByClassName('brightcoveplayer');
    let isModalOpen = false;

    function addPlayVideoOption(config: any) {
        const playLink = document.createElement('a');
        playLink.textContent = 'Play Video';
        playLink.className = 'play-video';
        playLink.setAttribute('href', '#');
        config.closest('.brightcoveplayer').append(playLink);
        config.parentElement.player.controls_ = false;
        config.parentElement.player.controlBar.el_.style.display = 'none';
        config.parentElement.player.bigPlayButton.el_.style.display = 'none';
    }

    function playVideo(option) {
        const vid = option.playElement.previousElementSibling.getElementsByTagName('video');
        addVideoModal(vid);
    }

    function addVideoModal(vid) {
        const modalDiv = document.createElement('div');
        modalDiv.className = `${video}__modal`;

        const closeModal = document.createElement('span');
        closeModal.className = `${video}__close`;
        closeModal.setAttribute('tabindex', '0');
        closeModal.setAttribute('aria-label', 'Close video player');

        const modalContent = document.createElement('div');
        modalContent.className = `${video}__modal-content`;

        modalDiv.append(closeModal, modalContent);

        const sourceElement = vid[0].parentNode;
        sourceElement.parentNode.appendChild(modalDiv);

        const modal = sourceElement.parentNode.getElementsByClassName(`${video}__modal`);
        const vidModal = modal.length == 1 ? modal[0] : null;

        vidModal.style.display = 'block';
        sourceElement.classList.add('modal-play-video');
        sourceElement.player.controls_ = true;
        sourceElement.player.controlBar.el_.style.display = 'flex';
        sourceElement.player.bigPlayButton.el_.style.display = '';
        isModalOpen = true;

        const spanClose = sourceElement.parentNode.getElementsByClassName(`${video}__close`)[0];
        spanClose.addEventListener('click', closeVideoModal);
        spanClose.addEventListener('keydown', function (event) {
            if (event.key === 'Enter' || event.keyCode === 13) {
                closeVideoModal();
            }
        });

        let keydownEventListener;
        if (isModalOpen === true) {
            keydownEventListener = function (event) {
                if (isModalOpen && event.key === 'Escape') {
                    closeVideoModal();
                    document.removeEventListener('keydown', keydownEventListener);
                }
                if (isModalOpen && event.key === 'Tab') {
                    event.preventDefault();
                    spanClose.focus();
                }
            };

            document.addEventListener('keydown', keydownEventListener);

            spanClose.addEventListener('focus', function () {
                document.removeEventListener('keydown', keydownEventListener);
            });
        }

        function closeVideoModal() {
            sourceElement.classList.remove('modal-play-video');
            vidModal.style.display = 'none';
            const modalToRemove = sourceElement.parentNode.querySelector('.video__modal');
            if (modalToRemove) {
                modalToRemove.remove();
            }
            sourceElement.player.controls_ = false;
            sourceElement.player.controlBar.el_.style.display = 'none';
            sourceElement.player.bigPlayButton.el_.style.display = 'none';
            vid[0].pause();
        }
    }

    function onDocumentReady() {
        if(videoPlayer && videoPlayer.length > 0) {
            window.onclick = e => {
                const playElements = document.querySelectorAll(videoSelectors.playOption);
                for (var i = 0; i < playElements.length && playElements[i]; i++) {
                    if(e.target == playElements[i]) {
                        playVideo({ playElement: playElements[i] });
                    }
                }
            };
        }
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

    const observedVideos = new Set();

    function handleMutation(mutationsList) {
      for (const mutation of mutationsList) {
        if (mutation.type === 'childList') {
          for (const node of mutation.addedNodes) {
            if (node.tagName === 'VIDEO' && !observedVideos.has(node)) {
              observedVideos.add(node);
              addPlayVideoOption(node);
            }
          }
        }
      }
    }

    const observer = new MutationObserver(handleMutation);
    observer.observe(document, { childList: true, subtree: true });

}());
