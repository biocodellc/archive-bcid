<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
   response.setHeader( "Pragma", "no-cache" );
   response.setHeader( "Cache-Control", "no-Store,no-Cache" );
   response.setDateHeader( "Expires", 0 );
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
    <title>BiSciCol Identifiers</title>
    <link rel="stylesheet" href="/bcid/css/jquery-ui.css" />
    <link rel="stylesheet" type="text/css" href="/bcid/css/biscicol.css"/>
</head>

<body>
<div class="section">
    <div class="sectioncontent" id="login">
        <h2>Login</h2>

        <c:if test="${pageContext.request.getQueryString() != null}">
         <!--
         Hardcoded return_to, client_id, and redirect_uri parameters for the web_nmnh application
         TODO: make this dynamic so we can login from BCID application itself
         -->
        <form method="POST" autocomplete="off" action="/id/authenticationService/loginLDAP?return_to=/id/authenticationService/oauth/authorize?client_id=GVK_t8pJrHsBhdgbKXNT&redirect_uri=http://nmnh-fims.si.edu/fims/rest/authenticationService/access_token/">
        </c:if>
        <c:if test="${pageContext.request.getQueryString() == null}">
        <form method="POST" autocomplete="off" action="/id/authenticationService/loginLDAP/">
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