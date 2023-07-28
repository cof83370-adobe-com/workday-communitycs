<%@ page import="com.adobe.granite.ui.components.Config" %>
<%@include file="/libs/granite/ui/global.jsp" %>

<%
    Config mCfg = cmp.getConfig();

    String SEARCH_PATHBROWSER_WRAPPER_CLASS = "workday-search-pathbrowser-wrapper-" + mCfg.get("name", String.class).substring(2);
    String WORKDAY_PREFIX = "workday.granite.ui.search.pathBrowser";
%>

<div class="<%=SEARCH_PATHBROWSER_WRAPPER_CLASS%>">
    <%--include ootb pathbrowser--%>
    <sling:include resourceType="/libs/granite/ui/components/foundation/form/pathbrowser"/>
</div>

<script>
    (function($){
        var wrapper = $(".<%=SEARCH_PATHBROWSER_WRAPPER_CLASS%>"),
            pathBrowser = wrapper.find("[data-init='pathbrowser']");

        if(_.isEmpty(pathBrowser)){
            return;
        }

        //set the search based pathbrowser loaders and renderers defined in search-based-pathbrowser.js
        pathBrowser.attr("data-autocomplete-callback", "<%=WORKDAY_PREFIX%>" + ".autocompletecallback");
        pathBrowser.attr("data-option-loader", "<%=WORKDAY_PREFIX%>" + ".optionLoader");
        pathBrowser.attr("data-option-renderer", "<%=WORKDAY_PREFIX%>" + ".optionRenderer");

    }(jQuery));
</script>