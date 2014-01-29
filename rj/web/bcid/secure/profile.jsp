<%@ include file="../header.jsp" %>

<div class="section">
    <div class="sectioncontent" id="login">
        <h2>Edit BCID Profile Information</h2>

        Fill in any information you would like to change.

        <ul>
            <li><b>Name</b> is optional.
            <li><b>Email</b> is optional.
            <li><b>Institution</b> is optional.
            <li><b>New Password</b> is optional
            <li><b>Old Password*</b> is required if entering a new password, otherwise new password will not save
        </ul>

        <form method="POST" action="/id/profileService">
            <table>
                <tr>
                    <td align="right">Name</td>
                    <td><input type="text" name="name"></td>
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