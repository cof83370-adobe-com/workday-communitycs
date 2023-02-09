(function() {
    var carouselSelectors = {
        currentPagination: '[class="cmp-carousel__actions__pagination"]'
    };

    function getCarouselPagination() {
        const activeItem = document.getElementsByClassName('cmp-carousel__item cmp-carousel__item--active');
        const activeCarouselItem = activeItem.length == 1 ? activeItem[0] : null;
        if(activeCarouselItem) {
            var activeSlide = activeCarouselItem.ariaLabel.replace('Slide ', '');
        }

        const carouselAction = document.getElementsByClassName('cmp-carousel__actions');
        const carouselActionItems = carouselAction.length == 1 ? carouselAction[0] : null;

        const activeSlideSpan = document.createElement('span');
        activeSlideSpan.append(activeSlide);
        activeSlideSpan.className = 'cmp-carousel__actions__pagination';

        if(carouselActionItems && (carouselActionItems.children.length > 1)) {
            if(carouselActionItems.querySelector(carouselSelectors.currentPagination)) {
                carouselActionItems.removeChild(carouselActionItems.children[1]);
            }
            carouselActionItems.insertBefore(activeSlideSpan, carouselActionItems.children[1]);
        }
    }

    function onDocumentReady() {
       getCarouselPagination();
       const previousAction = document.getElementsByClassName('cmp-carousel__action cmp-carousel__action--previous')[0];
       const nextAction = document.getElementsByClassName('cmp-carousel__action cmp-carousel__action--next')[0];

       window.addEventListener('load', function () {
           if(previousAction) {
                previousAction.addEventListener('click', getCarouselPagination);
           }
           if(nextAction) {
                nextAction.addEventListener('click', getCarouselPagination);
           }
       });
    }

    document.addEventListener('DOMContentLoaded', onDocumentReady);

}());
