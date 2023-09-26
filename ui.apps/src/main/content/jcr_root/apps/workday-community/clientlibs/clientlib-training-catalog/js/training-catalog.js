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
    if(course){
        var hbsOutput = populateCenterContainer(course.Report_Entry[0]);
        tcCenter.innerHTML = hbsOutput;
    }
  }

  if (tcRight !== undefined && tcRight !== null) {
    let populateRightContainer = Handlebars.compile(cleanHandlebars(tcRight.innerHTML));
    if(course){
        tcRight.innerHTML = populateRightContainer(course.Report_Entry[0]);
    }
  }
}

Handlebars.registerHelper("download", function (text, url) {
  var url = Handlebars.escapeExpression(url),
    text = Handlebars.escapeExpression(text)

  return new Handlebars.SafeString("<a class=\"cmp-download__title-link\" href='" + url + "'>" + text + "</a>");
});

Handlebars.registerHelper("button", function (text, url) {
  var url = Handlebars.escapeExpression(url),
    text = Handlebars.escapeExpression(text)
  return new Handlebars.SafeString("<a class=\"cmp-button\" href='" + url + "' target=\"_blank\"><span class=\"cmp-button__text\">Enroll" + text + "</span></a>");
});

Handlebars.registerHelper('isNotEmptyOrNull', function (value, options) {
  if (!value) { return options.inverse(this); }
  if (value.toUpperCase() == 'NULL') { return options.inverse(this); }
  return value.replace(/\s*/g, '').length === 0
    ? options.inverse(this) : options.fn(this);
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
