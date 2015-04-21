<%
   response.setHeader( "Pragma", "no-cache" );
   response.setHeader( "Cache-Control", "no-Store,no-Cache" );
   response.setDateHeader( "Expires", 0 );
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
    <link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/themes/flick/jquery-ui.css" />
    <link rel="stylesheet" type="text/css" href="/bcid/css/biscicol.css"/>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
    <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"></script>
    <script type="text/javascript" src="/bcid/js/bcid.js"></script>
</head>

<body>
<div class="section">
    <div class="sectioncontent" id="login">
        <h2>Login</h2>

         <!--
         Hardcoded return_to, client_id, and redirect_uri parameters for the web_nmnh application
         TODO: make this dynamic so we can login from BCID application itself
         -->
        <form method="POST" autocomplete="off">
            <table>
                <tr>
                    <td align="right">Username</td>
                    <td><input type="text" name="username" autofocus></td>
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

    </div>
</div>
 <div class="clearfooter"></div>
</div> <!—-End Container—>

</body>
</html>