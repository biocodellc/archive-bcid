<%@ include file="../header.jsp" %>

<div id="user" class="section">

    <div class="sectioncontent">
        <h2>${user} User Tools</h2>

        <br>
        <a class="expand-bcid-content" href="#">
            <img src="../images/right-arrow.png" id="bcid-arrow" class="img-arrow">BCIDs
        </a>

        <div class="toggle-bcid-content">
            <div id=listUserBCIDsAsTable style="overflow:auto;margin:0px 0px;">Loading groups</div>
        </div>

        <br>
        <a class="expand-expedition-content" href="#">
            <img src="../images/right-arrow.png" id="expedition-arrow" class="img-arrow">Expeditions
        </a>

        <div class="toggle-expedition-content">
            <div id=listUserExpeditionsAsTable style="overflow:auto;">Loading expeditions</div>
        </div>

        <c:if test="${projectAdmin != null}">
        <br>
        <a class="expand-manage-project-content" href="javascript:void(0)">
            <img src="../images/right-arrow.png" id="project-arrow" class="img-arrow">Manage Projects
        </a>

        <div class="toggle-manage-project-content" style="display:none;">
            <div id=listUsersProjects style="overflow:auto">

                <div class="expand-project-add-user-content" >
                    <a class="project-add-user" href="javascript:void(0)"><img src="../images/right-arrow.png" id="add-user-arrow" class="img-arrow">Add User</a>
                </div>

                <div class="toggle-project-add-user-content">
                    <div style="overflow:auto;">
                        <form method="POST" action="/id/userService/add/">
                            <table>
                                <tr>
                                    <td align="right">Project:</td>
                                    <td><select name="projectId" class="adminProjects"></select></td>
                                </tr>
                                <tr>
                                    <td align="right">Username:</td>
                                    <td><select name="userId" id="userList"<select></td>
                                </tr>
                                <c:if test="${param['addError'] != null}">
                                <tr>
                                    <td class="error" align="center" colspan="2">"Error occurred adding user to project"</td>
                                </tr>
                                </c:if>
                                <tr>
                                    <td></td>
                                    <td><input type="submit" value="Submit"></td>
                                </tr>
                            </table>
                        </form>
                    </div>
                </div>

                <div class="expand-project-create-user-content">
                    <a class="project-create-user" href="javascript:void(0)"><img src="../images/right-arrow.png" id="create-user-arrow" class="img-arrow">Create User</a>
                </div>

                <div class="toggle-project-create-user-content">
                    <div style="overflow:auto;">
                        <form method="POST" action="/id/userService/create/">
                            <table>
                                <tr>
                                    <td align="right">Project:</td>
                                    <td><select name="projectId" class="adminProjects"></select></td>
                                </tr>
                                <tr>
                                    <td align="right">Username:</td>
                                    <td><input type="text" name="username"></input></td>
                                </tr>
                                <tr>
                                    <td align="right">Password:</td>
                                    <td><input type="password" name="password"></input></td>
                                </tr>
                                <c:if test="${param['createError'] != null}">
                                <tr>
                                    <td class="error" align="center" colspan="2">"Error occurred creating new user"</td>
                                </tr>
                                </c:if>
                                <tr>
                                    <td></td>
                                    <td><input type="submit" value="Submit"></td>
                                </tr>
                            </table>
                        </form>
                    </div>
                </div>

            </div>
        </div>
        </c:if>

        <br>
        <a class="expand-profile-content" href="javascript:void(0)">
            <img src="../images/right-arrow.png" id="profile-arrow" class="img-arrow">User Profile
        </a>

        <div class="toggle-profile-content">
            <div id=listUserProfile style="overflow:auto;">Loading profile</div>
        </div>
    </div>
</div>

<script>
    // Populate BCIDs Table
    window.onload = populateDivFromService(
        "/id/groupService/listUserBCIDsAsTable",
        "listUserBCIDsAsTable",
        "Unable to load this user's BCIDs from Server");

    // Populate Expeditions Table
    window.onload = populateDivFromService(
        "/id/groupService/listUserExpeditionsAsTable",
        "listUserExpeditionsAsTable",
        "Unable to load this user's expeditions from Server");

    // Populate User Profile
    window.onload = populateDivFromService(
        "/id/userService/profile/listAsTable",
        "listUserProfile",
        "Unable to load this user's profile from the Server");

    // Populate Project Select
    window.onload = populateSelect("adminProjects");
    window.onload = populateSelect("userList");

    // Expand/Collapse BCIDs Section
    $('.expand-bcid-content').click(function(){
        if ($('.toggle-bcid-content').is(':hidden')) {
            $('#bcid-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#bcid-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-bcid-content').slideToggle('slow');
    });

    // Expand/Collapse Expeditions Section
    $('.expand-expedition-content').click(function(){
        if ($('.toggle-expedition-content').is(':hidden')) {
            $('#expedition-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#expedition-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-expedition-content').slideToggle('slow');
    });

    // Expand/Collapse Profile Section
    $('.expand-profile-content').click(function(){
        if ($('.toggle-profile-content').is(':hidden')) {
            $('#profile-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#profile-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-profile-content').slideToggle('slow');
    });
    // Expand/Collapse Project Section
    $('.expand-manage-project-content').click(function(){
        if ($('.toggle-manage-project-content').is(':hidden')) {
            $('#project-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#project-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-manage-project-content').slideToggle('slow');
    });
    // Expand/Collapse Project Section
    $('.expand-project-add-user-content').click(function(){
        if ($('.toggle-project-add-user-content').is(':hidden')) {
            $('#add-user-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#add-user-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-project-add-user-content').slideToggle('slow');
    });
    // Expand/Collapse Project Section
    $('.expand-project-create-user-content').click(function(){
        if ($('.toggle-project-create-user-content').is(':hidden')) {
            $('#create-user-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#create-user-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-project-create-user-content').slideToggle('slow');
    });
</script>

<%@ include file="../footer.jsp" %>