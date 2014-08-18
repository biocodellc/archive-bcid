<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
   response.setHeader( "Pragma", "no-cache" );
   response.setHeader( "Cache-Control", "no-cache" );
   response.setDateHeader( "Expires", 0 );
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
    <title>BCID Control Panel</title>
    <link rel="stylesheet" href="/bcid/css/jquery-ui.css" />
    <link rel="stylesheet" type="text/css" href="/bcid/css/biscicol.css"/>
    <script type="text/javascript" src="/bcid/js/jquery.min.js"></script>
    <script type="text/javascript" src="/bcid/js/bcid.js"></script>
    <script type="text/javascript" src="/bcid/js/dropit.js"></script>
    <script type="text/javascript" src="/bcid/js/pwstrength.js"></script>
    <script>$(document).ready(function() {$('.menu').dropit();});</script>
    <script src="/bcid/js/jquery-ui.min.js"></script>
</head>

<body>

<div id="container">

    <div id="header">

        <div style='float:left'><h1>Smithsonian FIMS Management Console</h1></div>

        <div style='float:right' id="loginLink">
            <c:if test="${user == null}">
                <a href="/bcid/login.jsp">Login</a>
            </c:if>
            <c:if test="${user != null}">
                <a href="/bcid/secure/profile.jsp">${user}</a> | <a href="/id/authenticationService/logout/">Logout</a>
            </c:if>
            <!--| <div class="link"><a href='/bcid/concepts.jsp'>Concepts</a></div>-->
            | <a href="https://code.google.com/p/bcid/">Help</a>
        </div>

        <div style="clear: both;"></div>

        <div style="overflow: auto;width: 100%;">
            <div class="link"><a href='/bcid/index.jsp'>Lookup</a></div>

            <div class="separator">|</div>

            <ul id="menu2" class="menu">
                <li><a href="#" class="btn">User Tools</a>

                    <c:if test="${user != null}">
                        <ul>
                            <li><a href='/bcid/concepts.jsp' class='enabled'>Lookup Concepts</a></li>
                            <li><a href='/bcid/secure/dataGroupCreator.jsp' class='enabled'>BCID Creator</a></li>
                            <li><a href='/bcid/secure/bcids.jsp' class='enabled'>Manage BCIDs</a></li>
                            <c:if test="${projectAdmin != null}">
                                <li><a href='/bcid/secure/projects.jsp' class='enabled'>Manage Projects</a></li>
                            </c:if>
                            <c:if test="${projectAdmin == null}">
                                <li><a href='#' class='disabled'>Manage Projects</a></li>
                            </c:if>
                            <li><a href='/bcid/secure/expeditions.jsp' class='enabled'>Manage Datasets</a></li>
                            <li><a href='/bcid/secure/profile.jsp' class='enabled'>User Profile</a></li>
                        </ul>
                    </c:if>

                    <c:if test="${user == null}">
                        <ul>
                            <li><a href='/bcid/concepts.jsp' class='enabled'>Lookup Concepts</a></li>
                            <li><a href='#' class='disabled'>BCID Creator</a></li>
                            <li><a href='#' class='disabled'>Manage BCIDs</a></li>
                            <li><a href='#' class='disabled'>Manage Projects</a></li>
                            <li><a href='#' class='disabled'>Manage Datasets</a></li>
                            <li><a href='#' class='disabled'>User Profile</a></li>
                         </ul>
                    </c:if>
                </li>
            </ul>

            <!--<div class="separator">|</div>
            <div class="link"><a href='/bcid/requestEZID.jsp'>Request EZID</a></div>-->
        </div>
    </div>
