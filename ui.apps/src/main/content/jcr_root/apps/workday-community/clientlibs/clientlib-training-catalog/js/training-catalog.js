const courseObj = "{\n    \"Report_Entry\": [\n        {\n            \"accessControl\": \"authenticated\",\n            \"communityUrl\": \"https://qa-content.workday.com/en-us/collections/learn/training-information/training-catalog/course-detail.html?title=Business Assets\",\n            \"creditsRange\": \"1.5\",\n            \"deliveryOptionsTitle1\": \"Business Assets - Learn Independent\",\n            \"deliveryOptionsTitle2\": \"NULL\",\n            \"deliveryOptionsTitle3\": \"NULL\",\n            \"deliveryOptionsTitle4\": \"NULL\",\n            \"deliveryOptionsUrl1\": \"https://wd5-impl.workday.com/workday5/d/inst/1$17815/17815$1642.htmld\",\n            \"deliveryOptionsUrl2\": \"NULL\",\n            \"deliveryOptionsUrl3\": \"NULL\",\n            \"deliveryOptionsUrl4\": \"NULL\",\n            \"description\": \"This course is designed for buyers, business asset specialists, and tax managers responsible for maintaining assets in Workday Financial Management.\\n\\nIn Workday, an asset is anything you want to track. Workday Business Assets combines the traditionally separate domains of fixed assets with the emerging importance of managing high-value and low-cost items. In this course, you will learn to set up the business process of Workday Business Assets functionality.\\n\\nThe goal is to get familiar with transactions so you can make better decisions for your Assets deployment.\\nThis course offering is being taught as Learn Independent. Students must meet additional requirements to attend Learn Independent. To learn more, visit the Learn Independent Resource Center on Workday Community. See the course info sheet for a more detailed course description.Note: This course will require approximately 12 hours of work, and the assigned training tenant will be available for 7 days (168 hours).\",\n            \"durationRange\": \"12 Hours\",\n            \"graphicUrl\": \"NULL\",\n            \"groupedTitle\": \"Business Assets\",\n            \"industry\": \"NULL\",\n            \"infosheetUrl\": \"NULL\",\n            \"library\": \"NULL\",\n            \"productLines\": \"Financial Management,Asset Management\",\n            \"relatedCourseTitle1\": \"NULL\",\n            \"relatedCourseTitle2\": \"NULL\",\n            \"relatedCourseTitle3\": \"NULL\",\n            \"relatedCourseTitle4\": \"NULL\",\n            \"relatedCourseUrl1\": \"NULL\",\n            \"relatedCourseUrl2\": \"NULL\",\n            \"relatedCourseUrl3\": \"NULL\",\n            \"relatedCourseUrl4\": \"NULL\",\n            \"reqPrereq\": \"NULL\",\n            \"roles\": \"Functional Lead,Workday Administrator\",\n            \"sugPrereq\": \"Financial Fundamentals or Workday Foundations\",\n            \"usingWorkday\": \"Training Tools \& Materials\",\n            \"videoUrl\": \"NULL\"\n        }\n    ]\n}";

function renderCourse() {
  const titleDiv = document.getElementById("courseDetailData");
  let courseData, course;
  if (titleDiv) {
    //courseData = titleDiv.getAttribute('data-model-property');
    courseData = courseObj;
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
