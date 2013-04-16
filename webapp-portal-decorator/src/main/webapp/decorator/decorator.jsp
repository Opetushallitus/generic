<%@ page import="fi.vm.sade.generic.common.EnhancedProperties" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.io.InputStream" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF8" %>
<%
    InputStream stream = new FileInputStream(new File(System.getProperty("user.home"), "oph-configuration/common.properties"));
    Properties props = new EnhancedProperties();
    props.load(stream);
    request.setAttribute("props", props);
%>
<!DOCTYPE html>
<html class="ltr yui3-js-enabled webkit ltr js chrome chrome25 chrome25-0 win" dir="ltr" lang="fi-FI">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Virkailijan työpöytä</title>
    <link type="text/css" href="${props["cas.service.liferay"]}/virkailija-theme/css/main.css" rel="stylesheet" />
    <%--<script src="../decorator/main.js" type="text/javascript"></script>--%>
    <script src="http://code.jquery.com/jquery-1.9.1.min.js" type="text/javascript"></script>
    <script type="text/javascript">
        $(function() {
            $.get('${pageContext.request.contextPath}/htmltemplate', function(htmltemplate){
                var tpl = $(htmltemplate);

                // copy elements
                $('.header-content').html(tpl.find('.header-content').html());
                $('#main-navigation').replaceWith(tpl.find('#main-navigation'));
                $('#footer').html(tpl.find('#footer').html());

                // bind navi actions
                $('#main-navigation .nav-item').bind('mouseover', function(){ this.children[1].style.display='block'; });
                $('#main-navigation .nav-item').bind('mouseout', function(){ this.children[1].style.display='none'; });

                // set img paths
                $("img").each(function(){
                    var src = $(this).attr("src");
                    if (src.indexOf("/virkailija-theme/")==0) {
                        $(this).attr("src", "${props["cas.service.liferay"]}" + src);
                    }
                });
            });
        });
    </script>

</head>
<body class=" yui3-skin-sam controls-visible signed-in private-page site">

<div id="siteheader">
    <div class="header-content">
        <%--<img class="margin-left-2" src="/virkailija-theme/images/general/opintopolkufi.png">--%>
        <%--<a class="margin-left-2" href="/group/virkailijan-tyopoyta">Virkailijan työpöytä</a>--%>
        <%--<a class="margin-left-2" href="/web/guest">Oppijan polku</a>--%>
        <%--<select class="margin-right-2 float-right">--%>
        <%--</select> <a class="margin-right-2 float-right" href="#">Kirjaudu ulos</a>--%>
    </div>
    <div class="clear"></div>
</div>
<div id="wrapper">
    <header id="banner" role="banner">
        <%--<nav id="main-navigation">--%>
        <nav class="sort-pages modify-pages" id="main-navigation">
            <ul class="navigation-list">
                <li class="selected nav-item">
                    <a href="#"></a>
                </li>
            </ul>
        </nav>
        <%--</nav>--%>
    </header>
    <div id="content">
        <nav class="site-breadcrumbs control-panel-category" id="breadcrumbs">
            <ul class="breadcrumbs breadcrumbs-horizontal lfr-component">
                <%--<li class="first"><span><a href="#">XXX</a></span>--%>
                <%--</li>--%>
                <%--<li class="last"><span><a href="#">YYY</a></span></li>--%>
            </ul>
        </nav>
        <%--<div class="portlet-boundary portlet-static portlet-static-end ">--%>
        <%--<div class="portlet-body"></div>--%>
        <%--</div>--%>
        <div class="columns-1" id="main-content" role="main" style="max-height: 5000px;">
            <div class="portlet-layout">
                <div class="portlet-column portlet-column-only" id="column-1">
                    <div class="portlet-layout">
                        <div class="portlet-dropzone portlet-column-content portlet-column-content-only" id="layout-column_column-1">
                            <div class="portlet-boundary portlet-static portlet-static-end portlet-borderless ">
                                <div class="portlet-body">
                                    <div class="portlet-borderless-container" style="">
                                        <div class="portlet-body" style="width: 100%;">

                                            <!--MAIN CONTENT-->
                                            <sitemesh:write property='body'/>

                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <form action="#" id="hrefFm" method="post" name="hrefFm"><span></span>
        </form>
    </div>
    <footer id="footer" class="offset-left-16-1 grid16-14">
        <%--<div id="footer-content-wrapper">--%>
        <%--<div class="grid16-4"></div>--%>
        <%--<div class="grid16-12">--%>
        <%--<div class="grid16-4 "></div>--%>
        <%--<div class="grid16-4"></div>--%>
        <%--<div class="grid16-4"></div>--%>
        <%--<div class="grid16-4"></div>--%>
        <%--<div class="grid16-8 footer-logo"><img src="../decorator/images/logo-opetus-ja-kulttuuriministerio.png"></div>--%>
        <%--<div class="grid16-8 footer-logo"><img src="../decorator/images/logo-oph.png"></div>--%>
        <%--</div>--%>
        <%--<div class="clear"></div>--%>
        <%--</div>--%>
    </footer>
    <div class="clear"></div>
</div>
</body>
</html>