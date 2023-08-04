(function() {
    const video = 'video';
    const videoSelectors = {
        playOption: '[class="play-video"]'
    };
    const videoPlayer = document.getElementsByClassName('brightcoveplayer');

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
        vid[0].muted = false;

        const spanClose = sourceElement.parentNode.getElementsByClassName(`${video}__close`)[0];
        spanClose.addEventListener('click', function(){
            sourceElement.classList.remove('modal-play-video');
            vidModal.style.display = 'none';
            sourceElement.parentNode.removeChild(modalDiv);
            sourceElement.player.controls_ = false;
            sourceElement.player.controlBar.el_.style.display = 'none';
            sourceElement.player.bigPlayButton.el_.style.display = 'none';
            vid[0].muted = true;
        });
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