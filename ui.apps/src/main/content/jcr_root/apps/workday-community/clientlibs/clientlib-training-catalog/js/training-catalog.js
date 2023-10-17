function renderCourse() {
  const titleDiv = document.getElementById("courseDetailData");
  let courseData, course;
  if (titleDiv) {
    courseData = titleDiv.getAttribute('data-model-property');
    if (stringValid(courseData)) {
      course = JSON.parse(courseData);
    }
  }
  const tcCenter = document.getElementById('training-catalog-center-container');
  const tcRight = document.getElementById('training-catalog-right-container');

  if (tcCenter !== undefined && tcCenter !== null) {
    let populateCenterContainer = Handlebars.compile(cleanHandlebars(tcCenter.innerHTML));
    if(course && course.Report_Entry && course.Report_Entry.length > 0){
        var hbsOutput = populateCenterContainer(course.Report_Entry[0]);
        tcCenter.innerHTML = hbsOutput;
    }
  }

  if (tcRight !== undefined && tcRight !== null) {
    let populateRightContainer = Handlebars.compile(cleanHandlebars(tcRight.innerHTML));
    if(course && course.Report_Entry && course.Report_Entry.length > 0){
        tcRight.innerHTML = populateRightContainer(course.Report_Entry[0]);
    }
  }

  if(course && course.Report_Entry && course.Report_Entry.length > 0 && course.Report_Entry[0].videoId !== 'NULL') {
    renderVideo(course.Report_Entry[0].videoId);
  }

  // Update active breadcrumb item
  const activeBreadcrumbElement = document.querySelector('.cmp-breadcrumb__item.cmp-breadcrumb__item--active [itemprop="name"]');
  const courseDetailTitleText = document.querySelector('#courseDetailData .cmp-title__text');

  if (activeBreadcrumbElement && courseDetailTitleText) {
    const activeBreadcrumbName = activeBreadcrumbElement.textContent;
    if (activeBreadcrumbName !== courseDetailTitleText.textContent) {
      activeBreadcrumbElement.textContent = courseDetailTitleText.textContent;
    }
  }
}

Handlebars.registerHelper("download", function (text, url) {
  var url = Handlebars.escapeExpression(url),
    text = Handlebars.escapeExpression(text)

  return new Handlebars.SafeString("<a class=\"cmp-download__title-link\" href='" + url + "' target=\"_blank\">" + text + "</a>");
});

Handlebars.registerHelper("button", function (text, url) {
  var url = Handlebars.escapeExpression(url),
    text = Handlebars.escapeExpression(text)
  return new Handlebars.SafeString("<a class=\"cmp-button\" href='" + url + "' target=\"_blank\"><button class=\"cmp-button__text\">Enroll " + text + "</button></a>");
});

Handlebars.registerHelper("relatedCourse", function (text, url) {
  var url = Handlebars.escapeExpression(url),
    text = Handlebars.escapeExpression(text)
  return new Handlebars.SafeString("<a class=\"cmp-related-info__item-link\" href='" + url + "' target=\"_blank\">" + "<span class=\"cmp-related-info__item-title\">" + text + "</span>" + "</a>");
});

Handlebars.registerHelper("image", function (url) {
  var url = Handlebars.escapeExpression(url)
  return new Handlebars.SafeString("<img src='" + url + "' class=\"cmp-image__image\" loading=\"lazy\" alt=\"Course detail image\">" + "</img>");
});

Handlebars.registerHelper('isNotEmptyOrNull', function (value, options) {
  if (!value) { return options.inverse(this); }
  if (value.toUpperCase() == 'NULL') { return options.inverse(this); }
  return value.replace(/\s*/g, '').length === 0
    ? options.inverse(this) : options.fn(this);
});

Handlebars.registerHelper('renderRelatedCourseHeading', function (value, options) {
  return (value.relatedCourseTitle1 == 'NULL'
         && value.relatedCourseTitle2 == 'NULL'
         && value.relatedCourseTitle3 == 'NULL'
         && value.relatedCourseTitle4 == 'NULL') ? options.inverse(this) : options.fn(this);
});

document.addEventListener('readystatechange', event => {
  if (event.target.readyState === 'complete') {
    renderCourse();
  }
});

function cleanHandlebars(str) {
  const re = /{<!-- -->{/gi;
  return str.replace(re, "{{");
}

function stringValid(str) {
  return (str !== undefined && str !== null && str.trim() !== '');
}

let newPlayer;
const playerData = {
  accountId: "6415684319001",
  playerId: "default"
};

// Build the player and place it in the HTML DOM
function renderVideo(videoId) {
  const playerHTML = `<video-js id="bc-video"
      data-video-id="${videoId}"
      data-account="${playerData.accountId}"
      data-player="${playerData.playerId}"
      data-embed="default" controls>
    </video-js>`;

  document.getElementById("brightcoveVideoId").innerHTML = playerHTML;

  const playerScript = document.createElement("script");
  playerScript.src = `https://players.brightcove.net/${playerData.accountId}/${playerData.playerId}_default/index.min.js`;
  document.body.appendChild(playerScript);
  playerScript.onload = callback;
}

// Initialize the player and start the video
function callback() {
  newPlayer = bc("bc-video");
  newPlayer.on("loadedmetadata", function() {
    newPlayer.pause();
  });
}
