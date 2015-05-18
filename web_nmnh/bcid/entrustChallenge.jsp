<%
   response.setHeader( "Pragma", "no-cache" );
   response.setHeader( "Cache-Control", "no-Store,no-Cache" );
   response.setDateHeader( "Expires", 0 );
%>
<%@ page isELIgnored="false" %>

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
    <div class="sectioncontent" id="challengeQuestions">
        <h2>Challenge Questions</h2>
        <form method="POST" autocomplete="off">
            <table>
                <tr>
                    <td align="right">${param.question_1}</td>
                    <td><input type="password" name="question_1" autofocus></td>
                </tr>
                <tr>
                    <td align="right">${param.question_2}</td>
                    <td><input type="password" name="question_2"></td>
                </tr>
                <tr>
                    <td></td>
                    <td><input type="hidden" name="userid" value="${param.userid}"></td>
                <tr>
                <!-- TODO: get the following jstl to work
                <c:forEach var="par" items="${paramValues}">
                    <c:choose>
                        <c:when test="${par.key.startsWith('question')}">

                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td></td>
                                <td><input type="hidden" name="${par.key}" value="${par.value}"></td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
                --!>
                <tr></tr>
                <tr>
                    <td></td>
                    <td class="error" align="center"></td>
                </tr>
                <tr>
                    <td></td>
                    <td ><input type="button" value="Submit" onclick="challengeResponse();"></td>
                </tr>
            </table>
        </form>

    </div>
</div>
 <div class="clearfooter"></div>
</div> <!—-End Container—>

</body>
</html>