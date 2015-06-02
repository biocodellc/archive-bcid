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
     <script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
    <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"></script>
    <script type="text/javascript" src="/bcid/js/bcid.js"></script>
</head>

<body>
<div class="section">
    <div class="sectioncontent" id="login">
        <h2>Login</h2>
        <form method="POST" autocomplete="off">

            <table>
                <tr>
                    <td align="right">Username</td>
                    <td><input type="text" name="username" autofocus></td>
                </tr>
                <tr>
                    <td align="right">Password</td>
                    <td><input type="password" name="password" autocomplete="off"></td>
                </tr>
                <tr></tr>
                <tr>
                    <td></td>
                    <td class="error" align="center"></td>
                </tr>
                <tr>
                    <td></td>
                    <td ><input type="button" value="Submit" onclick="login();"></td>
                </tr>
            </table>
        </form>
    <p>
    For assistance with your account login or challenge questions call (202) 633-4000 or
    visit <a href="http://myid.si.edu/">http://myid.si.edu/</a>
    </div>
</div>
 <div class="clearfooter"></div>
</div> <!—-End Container—>

</body>
</html>
