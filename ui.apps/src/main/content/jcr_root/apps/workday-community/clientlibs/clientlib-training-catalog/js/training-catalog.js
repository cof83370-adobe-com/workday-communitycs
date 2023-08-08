const course = {
    "Report_Entry": [
        {
            "accessControl": "authenticated",
            "communityUrl": "NA",
            "creditsRange": "1.0",
            "deliveryOptionsTitle1": "Recruiting Transactions - Learn Independent",
            "deliveryOptionsTitle2": "NA",
            "deliveryOptionsTitle3": "Recruiting Transactions - Learn Virtual",
            "deliveryOptionsTitle4": "NA",
            "deliveryOptionsUrl1": "https://wd5-impl.workday.com/workday1/d/inst/1$17815/17815$1702.htmld",
            "deliveryOptionsUrl2": " ",
            "deliveryOptionsUrl3": "https://wd5-impl.workday.com/workday1/d/inst/1$17815/17815$2015.htmld",
            "deliveryOptionsUrl4": " ",
            "description": "This course is designed for recruiters and Workday Recruiting users who will perform day-to-day recruiting transactions.Through hands-on activities and demonstrations, you will learn how to execute transactions spanning from creating job requisitions to applying to jobs and managing lists of applicants. In this course, you will experience Workday Recruiting from the perspective of both recruiters and candidates.Individuals who are responsible for managing the configuration of the Workday Recruiting application should attend the Recruiting Fundamentals course instead of Recruiting Transactions. See the course info sheet for a more detailed course description.",
            "durationRange": "10 Hours",
            "graphicUrl": "NA",
            "groupedTitle": "Recruiting Transactions",
            "infosheetUrl": "https://experienceleague.adobe.com/docs/experience-manager-65/assets/JCR_query_cheatsheet-v1.1.pdf",
            "library": "NA",
            "productLines": "Human Capital Management,Talent Management - Talent Acquisition",
            "relatedCourseTitle1": "NA",
            "relatedCourseTitle2": "NA",
            "relatedCourseTitle3": "NA",
            "relatedCourseTitle4": "NA",
            "relatedCourseUrl1": "NA",
            "relatedCourseUrl2": "NA",
            "relatedCourseUrl3": "NA",
            "relatedCourseUrl4": "NA",
            "reqPrereq": "NA",
            "roles": "Business User",
            "sugPrereq": "NA",
            "tracksPathways": "NA",
            "videoUrl": " ",
            "workdayPro": "NA"
        }
    ]
};

function renderCourse() {
  const tcCenter = document.getElementById('training-catalog-center-container');
  const tcRight = document.getElementById('training-catalog-right-container');
  
  if (tcCenter !== undefined && tcCenter !== null) {
     let populateCenterContainer = Handlebars.compile(cleanHandlebars(tcCenter.innerHTML));
     var hbsOutput = populateCenterContainer(course.Report_Entry[0]);
     tcCenter.innerHTML = hbsOutput;
  }

  if (tcRight !== undefined && tcRight !== null) {
    let populateRightContainer = Handlebars.compile(cleanHandlebars(tcRight.innerHTML));
    tcRight.innerHTML = populateRightContainer(course.Report_Entry[0]);
 }

}

Handlebars.registerHelper("download", function(text, url) {
  var url = Handlebars.escapeExpression(url),
      text = Handlebars.escapeExpression(text)
      
 return new Handlebars.SafeString("<a class=\"cmp-download__title-link\" href='" + url + "'>" + text +"</a>");
});

Handlebars.registerHelper("button", function(text, url) {
  var url = Handlebars.escapeExpression(url),
      text = Handlebars.escapeExpression(text)
  return new Handlebars.SafeString("<a class=\"cmp-button\" href='"+ url +"'><span class=\"cmp-button__text\">Enroll" + text + "</span></a>");
});

Handlebars.registerHelper('isNotEmpty', function (value, options) {
	if (!value) { return options.inverse(this); }
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
