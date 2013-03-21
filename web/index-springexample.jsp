<%@ page session="false" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
  <head>
      <meta http-equiv="content-type" content="text/html; charset=UTF-8">
      <link rel="stylesheet" href="<c:url value='/static/css/tutorial.css'/>" type="text/css" />
      <title>Home Page</title>
  </head>
<body>

<div style='text-align:right' id="loginLink">
    <sec:authorize ifNotGranted="user">
        <%//Display Login -- force it by when user clicks Login to redirect to secure area which triggers login  %>
        <a href="secure/index.jsp">Login</a>
    </sec:authorize>

    <sec:authorize access="hasRole('user')">
        <%= request.getRemoteUser() %> | <a href="j_spring_security_logout">Logout</a></p>
    </sec:authorize>
</div>

<h1>Welcome!</h1>


<sec:authorize url='/secure/index.jsp'>
    You are logged in and can view "/secure" URLs.
</sec:authorize>

<p>

<a href="secure/index.jsp">Links to secure content</a></p>


</body>
</html>