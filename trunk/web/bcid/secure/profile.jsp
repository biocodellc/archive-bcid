<%@ include file="../header.jsp" %>

<div class="section">
    <div class="sectioncontent" id="login">
        <h2>Edit BCID Profile Information</h2>

        <c:if test="${pageContext.request.getQueryString() != null}">
        <form method="POST" action="/id/profileService?${pageContext.request.getQueryString()}">
        </c:if>
        <c:if test="${pageContext.request.getQueryString() == null}">
        <form method="POST" action="/id/profileService">
        </c:if>
            <table>
                <tr>
                    <td align="right">First Name</td>
                    <td><input type="text" name="firstName"></td>
                </tr>
                <tr>
                    <td align="right">Last Name</td>
                    <td><input type="text" name="lastName"></td>
                </tr>
                <tr>
                    <td align="right">Email</td>
                    <td><input type="email" name="email"></td>
                </tr>
                <tr>
                    <td align="right">Institution</td>
                    <td><input type="text" name="institution"></td>
                </tr>
                <tr>
                    <td align="right">New Password</td>
                    <td><input type="password" name="new_password"></td>
                </tr>
                <tr>
                    <td align="right">Old Password*</td>
                    <td><input type="password" name="old_password"></td>
                </tr>
                <c:if test="${param['error'] != null}">
                <tr>
                    <td></td>
                    <td class="error" align="center">${param['error']}</td>
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

<script>
    // Populate form data
    window.onload = populateProfileForm();
</script>

<%@ include file="../footer.jsp" %>