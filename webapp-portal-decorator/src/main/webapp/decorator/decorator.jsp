<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.io.File" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF8" %>
<%
    InputStream stream = new FileInputStream(new File(System.getProperty("user.home"), "oph-configuration/common.properties"));
    Properties props = new Properties();
    props.load(stream);
    request.setAttribute("props", props);
%>
<!DOCTYPE html>
<!-- saved from url=(0077)${props['cas.service.liferay']}/group/virkailijan-tyopoyta/eops-ja-etutkinnot1 -->
<html class="ltr yui3-js-enabled webkit ltr js chrome chrome25 chrome25-0 win" dir="ltr" lang="fi-FI">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Virkailijan työpöytä</title>
    <!--<link href="${props['cas.service.liferay']}/virkailija-theme/images/favicon.ico" rel="Shortcut Icon">-->
    <!--<link href="../decorator/liferaypagetest_files/main.css" rel="stylesheet" type="text/css">-->
    <!--<script src="../decorator/liferaypagetest_files/everything.jsp" type="text/javascript"></script>-->
    <!--<link class="lfr-css-file" href="../decorator/liferaypagetest_files/main(1).css" rel="stylesheet" type="text/css">-->
    <!--<link id="aui_3_4_0_1_63" type="text/css" rel="stylesheet" href="../decorator/liferaypagetest_files/saved_resource" charset="utf-8">-->
    <!--<link id="aui_3_4_0_1_77" type="text/css" rel="stylesheet" href="../decorator/liferaypagetest_files/saved_resource(1)" charset="utf-8">-->
    <link type="text/css" href="../decorator/all.css" rel="stylesheet" />
    <script src="../decorator/main.js" type="text/javascript"></script>
</head>
<body class=" yui3-skin-sam controls-visible signed-in private-page site">
<div id="heading">
    <div id="tools-left" class="left">
        <div class="tools-item"> <!-- old: <a href="/group/guest">Virkailijan työpöytä</a> --> <a
                href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta">Virkailijan työpöytä</a></div>
        <div class="tools-item"><a href="${props['cas.service.liferay']}/web/guest">Oppijan polku</a></div>
    </div>
    <div id="tools-right" class="right">
        <div class="tools-item"><a href="${props['web.url.cas']}/logout?service=${props['cas.service.liferay']}/c/portal/logout">Kirjaudu ulos</a>
        </div>
        <div class="tools-item virkailija-select"><select>
            <option><%= request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "NULL USER!!!" %></option>
            <option> * Omat tiedot</option>
            <option> * Omat asetukset</option>
            <option>-----</option>
            <option>Vaihda käyttäjärooleja</option>
            <option> * Rooli1</option>
            <option> * Rooli2</option>
            <option>-----</option>
            <option>Vaihda kieltä</option>
            <option> * Suomi</option>
            <option> * Svensk</option>
            <option> * English</option>
        </select></div>
        <div class="tools-item">
            <form action="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/eops-ja-etutkinnot1?p_p_auth=1RyZ7Nmv&p_p_id=77&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_77_struts_action=%2Fjournal_content_search%2Fsearch"
                  class="aui-form" method="post" name="_77_fm" onsubmit="submitForm(this); return false;"><span
                    class="aui-field aui-field-text aui-field-inline lfr-search-keywords"> <span class="aui-field-content"> <span
                    class="aui-field-element "> <input class="aui-field-input aui-field-input-text" id="keywords" name="keywords" title="Etsi web artikkeleita"
                                                       type="text" value="Hae..."
                                                       onblur="if (this.value == &#39;&#39;) { this.value = &#39;\u0048\u0061\u0065\u002e\u002e\u002e&#39;; }"
                                                       onfocus="if (this.value == &#39;\u0048\u0061\u0065\u002e\u002e\u002e&#39;) { this.value = &#39;&#39;; }"
                                                       size="30"> </span> </span> </span> <span
                    class="aui-field aui-field-text aui-field-inline lfr-search-button"> <span class="aui-field-content"> <span
                    class="aui-field-element "> <input class="aui-field-input aui-field-input-text" id="search" name="search" type="image" value="" alt="search"
                                                       src="../decorator/images/search.png"> </span> </span> </span></form>
        </div>
        <div class="tools-item"><a href="#">Edistynyt haku</a></div>
    </div>
    <div class="clear"></div>
</div>
<div id="wrapper">
    <header id="banner" role="banner">
        <nav class="sort-pages modify-pages" id="main-navigation">
            <ul class="navigation-list">
                <li class="nav-item"><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/koti"> Koti </a></li>
                <li class="nav-item"><a href="#" onclick="return false;">
                    Organisaatiot </a>

                    <div class="sub-navigation">
                        <ul>
                            <%--<li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/organisaatiot"> Organisaation tietojen ylläpito </a></li>--%>
                            <li><a href="${props['cas.service.organisaatio-app']}/organisaatioapp/app"> Organisaation tietojen ylläpito </a></li>
                        </ul>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="nav-item"><a href="#" onclick="return false;">
                    Suunnittelu ja tarjonta </a>

                    <div class="sub-navigation">
                        <ul>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/eops-ja-etutkinnot1"> eOps ja eTutkinnot </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/koulutuksen-toteutus-ja-hakukohde"> Koulutusten ja
                                hakukohteiden ylläpito </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/valintaperustekuvausten-yllapito1"> Valintaperustekuvausten
                                ylläpito </a></li>
                        </ul>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="nav-item"><a href="#" onclick="return false;"> Haut </a>

                    <div class="sub-navigation">
                        <ul>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/hakujen-yllapito"> Haun ylläpito </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/hakulomakkeet"> Hakulomakkeen ylläpito </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/valintaperusteet"> Valintaperustekaavojen ylläpito </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/hakemusten-kasittely"> Hakemusten käsittely </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/suoritustietojen-ja-arvosanojen-kasittely"> Suoritustietojen
                                ja arvosanojen käsittely </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/hakijaraportit"> Hakijaraportit </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/ilmoitukset-ja-muistutukset"> Ilmoitukset ja
                                muistutukset </a></li>
                        </ul>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="nav-item"><a href="#" onclick="return false;">
                    Valinnat </a>

                    <div class="sub-navigation">
                        <ul>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/pistetietojen-yllapito"> Pistetietojen ylläpito </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/valinta"> Valintatietojen ylläpito </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/valintatulos"> Valintatulokset </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/sijoittelu"> Sijoittelu ja valintaesitys </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/valintaraportit"> Valintaraportit </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/opiskelupaikan-vastaanotto-ja-ilmoittautuminen">
                                Opiskelupaikan vastaanotto ja ilmoittautuminen </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/vastaanotto-ja-ilmoittautumisraportit"> Vastaanotto- ja
                                ilmoittautumisraportit </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/ilmoitukset-ja-muistutukset1"> Ilmoitukset ja
                                muistutukset </a></li>
                        </ul>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="nav-item"><a href="#" onclick="return false;">
                    Koulutustiedotus </a>

                    <div class="sub-navigation">
                        <ul>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/sisaltoartikkeleiden-yllapito"> Sisältöartikkelit </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/tiedostonhallinta"> Tiedostot ja kuvat </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/koulutusalakuvausten-yllapito"> Koulutusalakuvaukset </a>
                            </li>
                        </ul>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="nav-item"><a href="#" onclick="return false;">
                    Käytönhallinta </a>

                    <div class="sub-navigation">
                        <ul>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/yhteystietotyypit"> Yhteystietotyypit </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/henkilotietojenhallinta"> Henkilötietojen käsittely </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/kayttooikeusanomukset"> Käyttöoikeusanomukset </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/koosteroolienyllapito"> Käyttöoikeusryhmien hallinta </a>
                            </li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/kayttoohjeiden-yllapito"> Käyttöohjeiden ylläpito </a></li>
                            <%--<li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/koodisto"> Koodistojen ylläpito </a></li>--%>
                            <li><a href="${props['cas.service.koodisto-app']}/koodistoapp/app"> Koodistojen ylläpito </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/web-analytiikka"> Web-analytiikkaraportit </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/raportit-ja-tilastointi"> Muut raportit </a></li>
                        </ul>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="nav-item"><a href="#" onclick="return false;">
                    Tukipalvelut </a>

                    <div class="sub-navigation">
                        <ul>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/tyoryhmat"> Työryhmät </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/koulutusmateriaalit"> Koulutusmateriaalit </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/ohjeet"> Ohjeet </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/tietolahteet"> Tietolähteet </a></li>
                        </ul>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="nav-item"><a href="#" onclick="return false;"> Omat
                    sivut </a>

                    <div class="sub-navigation">
                        <ul>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/omat-tiedot"> Omat tiedot </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/omat-asetukset"> Omat asetukset </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/omat-kirjanmerkit"> Omat kirjanmerkit </a></li>
                            <li><a href="${props['cas.service.liferay']}/group/virkailijan-tyopoyta/omatviestit"> Omat viestit </a></li>
                        </ul>
                    </div>
                    <div class="clear"></div>
                </li>
            </ul>
            <div class="clear"></div>
        </nav>
    </header>
    <div id="content">
        <nav class="site-breadcrumbs control-panel-category" id="breadcrumbs">
            <ul class="breadcrumbs breadcrumbs-horizontal lfr-component">
                <li class="first"><span><a href="#">XXX</a></span>
                </li>
                <li class="last"><span><a href="#">YYY</a></span></li>
            </ul>
        </nav>
        <div id="p_p_id_103_" class="portlet-boundary portlet-boundary_103_ portlet-static portlet-static-end "><span id="p_103"></span>

            <div class="portlet-body"></div>
        </div>
        <div class="columns-1" id="main-content" role="main" style="max-height: 5000px;">
            <!--MAIN CONTENT-->
            <sitemesh:write property='body'/>
        </div>
        <form action="#" id="hrefFm" method="post" name="hrefFm"><span></span>
        </form>
    </div>
    <footer id="footer" role="contentinfo">
        <div id="footer-content-wrapper">
            <div class="grid16-4"></div>
            <div class="grid16-12">
                <div class="grid16-4 "></div>
                <div class="grid16-4"></div>
                <div class="grid16-4"></div>
                <div class="grid16-4"></div>
                <div class="grid16-8 footer-logo"><img src="../decorator/images/logo-opetus-ja-kulttuuriministerio.png"></div>
                <div class="grid16-8 footer-logo"><img src="../decorator/images/logo-oph.png"></div>
            </div>
            <div class="clear"></div>
        </div>
    </footer>
</div>
</body>
</html>