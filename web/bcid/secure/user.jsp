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
        <a class="expand-project-content" href="#">
            <img src="../images/right-arrow.png" id="project-arrow" class="img-arrow">Projects
        </a>

        <div class="toggle-project-content">
            <div id=listUserProjectsAsTable style="overflow:auto;">Loading projects</div>
        </div>

        <c:if test="${expeditionAdmin != null}">
        <br>
        <a class="expand-manage-expedition-content" href="javascript:void(0)">
            <img src="../images/right-arrow.png" id="expedition-arrow" class="img-arrow">Manage Expeditions
        </a>

        <div class="toggle-manage-expedition-content" style="display:none;">
            <div id=listUsersExpeditions style="overflow:auto">

                <div class="expand-expedition-add-user-content" >
                    <a class="expedition-add-user" href="javascript:void(0)"><img src="../images/right-arrow.png" id="add-user-arrow" class="img-arrow">Add User</a>
                </div>

                <div class="toggle-expedition-add-user-content">
                    <div style="overflow:auto;">
                        <form method="POST" action="/id/userService/add/">
                            <table>
                                <tr>
                                    <td align="right">Expedition:</td>
                                    <td><select name="expeditionId" class="adminExpeditions"></select></td>
                                </tr>
                                <tr>
                                    <td align="right">Username:</td>
                                    <td><select name="userId" id="userList"<select></td>
                                </tr>
                                <c:if test="${param['addError'] != null}">
                                <tr>
                                    <td class="error" align="center" colspan="2">"Error occurred adding user to expedition"</td>
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

                <div class="expand-expedition-create-user-content">
                    <a class="expedition-create-user" href="javascript:void(0)"><img src="../images/right-arrow.png" id="create-user-arrow" class="img-arrow">Create User</a>
                </div>

                <div class="toggle-expedition-create-user-content">
                    <div style="overflow:auto;">
                        <form method="POST" action="/id/userService/create/">
                            <table>
                                <tr>
                                    <td align="right">Expedition:</td>
                                    <td><select name="expeditionId" class="adminExpeditions"></select></td>
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

    // Populate Projects Table
    window.onload = populateDivFromService(
        "/id/groupService/listUserProjectsAsTable",
        "listUserProjectsAsTable",
        "Unable to load this user's projects from Server");

    // Populate User Profile
    window.onload = populateDivFromService(
        "/id/groupService/listUserProfile",
        "listUserProfile",
        "Unable to load this user's profile from the Server");

    // Populate Expedition Select
    window.onload = populateSelect("adminExpeditions");
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

    // Expand/Collapse Projects Section
    $('.expand-project-content').click(function(){
        if ($('.toggle-project-content').is(':hidden')) {
            $('#project-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#project-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-project-content').slideToggle('slow');
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
    // Expand/Collapse Expedition Section
    $('.expand-manage-expedition-content').click(function(){
        if ($('.toggle-manage-expedition-content').is(':hidden')) {
            $('#expedition-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#expedition-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-manage-expedition-content').slideToggle('slow');
    });
    // Expand/Collapse Expedition Section
    $('.expand-expedition-add-user-content').click(function(){
        if ($('.toggle-expedition-add-user-content').is(':hidden')) {
            $('#add-user-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#add-user-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-expedition-add-user-content').slideToggle('slow');
    });
    // Expand/Collapse Expedition Section
    $('.expand-expedition-create-user-content').click(function(){
        if ($('.toggle-expedition-create-user-content').is(':hidden')) {
            $('#create-user-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#create-user-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-expedition-create-user-content').slideToggle('slow');
    });
</script>

<%@ include file="../footer.jsp" %>