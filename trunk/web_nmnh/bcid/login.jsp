<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
    <title>BiSciCol Identifiers</title>
    <link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/themes/flick/jquery-ui.css" />
    <link rel="stylesheet" type="text/css" href="/bcid/css/biscicol.css"/>
</head>

<body>
<div class="section">
    <div class="sectioncontent" id="login">
        <h2>Login</h2>

        <c:if test="${pageContext.request.getQueryString() != null}">
        <form method="POST" action="/id/authenticationService/login?${pageContext.request.getQueryString()}">
        </c:if>
        <c:if test="${pageContext.request.getQueryString() == null}">
        <form method="POST" action="/id/authenticationService/login/">
        </c:if>
            <table>
                <tr>
                    <td align="right">Username</td>
                    <td><input type="text" name="username" autofocus></td>
                </tr>
                <tr>
                    <td align="right">Password</td>
                    <td><input type="password" name="password"></td>
                </tr>
                <tr>
                    <td></td>
                    <td align="center"><a href="/bcid/reset.jsp">(forgot password)</a></td>
                </tr>
                <c:if test="${param['error'] != null}">
                <tr></tr>
                <tr>
                    <td></td>
                    <td class="error" align="center">Bad Credentials</td>
                </tr>
                </c:if>
                <tr>
                    <td></td>
                    <td ><input type="submit" value="Submit"></td>
                </tr>
            </table>
        </form>

    </div>
</div>
 <div class="clearfooter"></div>
</div> <!—-End Container—>

</body>
</html>