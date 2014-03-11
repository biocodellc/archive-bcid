<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
    <title>BiSciCol Identifier Tools</title>
    <link rel="stylesheet" type="text/css" href="/bcid/css/biscicol.css"/>
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
    <script type="text/javascript" src="/bcid/js/bcid.js"></script>
</head>

<body>

<div id="container">

    <div id="header">

        <div style='float:left'><h1>Biocode Commons Identifiers</h1></div>

        <div style='float:right' id="loginLink">
            <c:if test="${user == null}">
                <a href="/bcid/login.jsp">Login</a>
            </c:if>
            <c:if test="${user != null}">
                <a href="/bcid/secure/user.jsp">${user}</a> | <a href="/id/authenticationService/logout/">Logout</a>
            </c:if>
            | <div class="link"><a href='/bcid/concepts.jsp'>Concepts</a></div>
            | <a href="https://code.google.com/p/bcid/">Help</a>
        </div>

        <div style="clear: both;"></div>

        <div style="overflow: auto;width: 100%;">
            <div class="link"><a href='/bcid/index.jsp'>Lookup</a></div>
            <div class="separator">|</div>

            <c:if test="${user != null}">
                <div class="link"><a href='/bcid/secure/dataGroupCreator.jsp'>BCID Creator</a></div>

                <!--<div class="separator">|</div><div class="link"><a href='/bcid/secure/creator.jsp'>Element Creator</a></div>-->

                <div class="separator">|</div>

                <div class="link"><a href='/bcid/secure/bcids.jsp'>Manage BCIDs</a></div>

                <div class="separator">|</div>

                <div class="link"><a href='/bcid/secure/projects.jsp'>Manage Projects</a></div>

                <div class="separator">|</div>

                <div class="link"><a href='/bcid/secure/expeditions.jsp'>Manage Expeditions</a></div>

                <div class="separator">|</div>

                <div class="link"><a href='/bcid/secure/user.jsp'>User Profile</a></div>
            </c:if>

            <c:if test="${user == null}">
                <div class="disabledlink">BCID Creator</div>

                <!--<div class="separator">|</div><div class="disabledlink">Element Creator</div>-->

                <div class="separator">|</div>

                <div class="disabledlink">Manage BCIDs</div>

                <div class="separator">|</div>

                <div class="disabledlink">Manage Projects</div>

                <div class="separator">|</div>

                <div class="disabledlink">Manage Expeditions</div>

                <div class="separator">|</div>

                <div class="disabledlink">User Profile</div>
            </c:if>

            <!--<div class="separator">|</div>
            <div class="link"><a href='/bcid/requestEZID.jsp'>Request EZID</a></div>-->
        </div>
    </div>
