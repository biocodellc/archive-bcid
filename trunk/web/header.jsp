<%@ page session="false" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

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
            <sec:authorize ifNotGranted="user">
                <a href="/bcid/secure/index.jsp">Login</a>
            </sec:authorize>
            <sec:authorize access="hasRole('user')">
                <a href="/bcid/secure/user.jsp"><%= request.getRemoteUser() %></a> | <a href="/bcid/j_spring_security_logout">Logout</a>
            </sec:authorize>
            | <a href="https://code.google.com/p/bcid/">Help</a>
        </div>

        <div style="clear: both;"></div>

        <div style="overflow: auto;width: 100%;">
            <div class="link"><a href='/bcid/index.jsp'>Resolver</a></div>
            <div class="separator">|</div>

            <sec:authorize access="hasRole('user')">
                <div class="link"><a href='/bcid/secure/dataGroupCreator.jsp'>Data Group Creator</a></div>
                <div class="separator">|</div>
                <div class="link"><a href='/bcid/secure/creator.jsp'>Element Creator</a></div>
            </sec:authorize>

            <sec:authorize ifNotGranted="user">
                <div class="disabledlink">Data Group Creator</div>
                <div class="separator">|</div>
                <div class="disabledlink">Element Creator</div>
            </sec:authorize>


            <div class="separator">|</div>
            <div class="link"><a href='/bcid/concepts.jsp'>Concepts</a></div>

            <!--<div class="separator">|</div>
            <div class="link"><a href='/bcid/requestEZID.jsp'>Request EZID</a></div>-->
        </div>
    </div>
