<%@ include file="../header.jsp" %>

<div class="section">

    <div class="sectioncontent">
    <!--     <h2>${user}s Projects</h2>

       <c:if test="${projectAdmin != null}">
        <br>
        <a class="expand-manage-project-content" href="javascript:void(0)">
            <img src="../images/right-arrow.png" id="project-arrow" class="img-arrow">Manage Projects
        </a>

        <div class="toggle-manage-project-content" style="display:none;">
            <div id=listUsersProjects style="overflow:auto">

                <a class="expand-project-config-content" href="#">
                    <img src="../images/right-arrow.png" id="project-config-arrow" class="img-arrow">Project Configuration
                </a>

                <div class="toggle-project-config-content">
                    <div id="projectConfig" style="overflow:auto;">
                    <table>
                        <tr>
                            <td align="right">Project:</td>
                            <td><select name="projectId" class="adminProjects"></select></td>
                        </tr>
                        <tr>
                            <td align="right">Title:</td>
                            <td id="projectTitle">Loading</td>
                        </tr>
                        <tr>
                            <td align="right">Abstract:</td>
                            <td id="projectAbstract">Loading</td>
                        </tr>
                        <tr>
                            <td align="right">Validation XML:</td>
                            <td id="projectValidationXML">Loading</td>
                        </tr>
                        <tr>
                            <td></td>
                            <td><a href="#">Edit Project Configuration</a></td>
                        </tr>
                    </table>
                    </div>
                </div>

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
        --!>
    </div>
</div>

<script>
 //   var projectConfigSelect = $('.adminProjects', '#projectConfig')

    $(document).ready(function() {

        populateProjectPage("${user}");

        // Populate Project Select
 /*       populateSelect("adminProjects");
        populateSelect("userList");

        projectConfigSelect.change(function() {
            updateProjectConfig(projectConfigSelect);
        });
        alert(projectConfigSelect.val());
        updateProjectConfig(projectConfigSelect);
        */
    })
 /*
    // Expand/Collapse Project Section
    $('.expand-manage-project-content').click(function(){
        if ($('.toggle-manage-project-content').is(':hidden')) {
            $('#project-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#project-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-manage-project-content').slideToggle('slow');
    });
    // Expand/Collapse Add User Section
    $('.expand-project-add-user-content').click(function(){
        if ($('.toggle-project-add-user-content').is(':hidden')) {
            $('#add-user-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#add-user-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-project-add-user-content').slideToggle('slow');
    });
    // Expand/Collapse Create User Section
    $('.expand-project-create-user-content').click(function(){
        if ($('.toggle-project-create-user-content').is(':hidden')) {
            $('#create-user-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#create-user-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-project-create-user-content').slideToggle('slow');
    });
    // Expand/Collapse Project Configuration Section
    $('.expand-project-config-content').click(function(){
        if ($('.toggle-project-config-content').is(':hidden')) {
            $('#project-config-arrow').attr("src","../images/down-arrow.png");
        } else {
            $('#project-config-arrow').attr("src","../images/right-arrow.png");
        }

        $('.toggle-project-config-content').slideToggle('slow');
    });
*/
</script>

<%@ include file="../footer.jsp" %>