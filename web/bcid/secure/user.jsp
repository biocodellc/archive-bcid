<%@ include file="../header.jsp" %>

<div id="user" class="section">

    <div class="sectioncontent">
        <h2><%= request.getRemoteUser() %> User Tools</h2>

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
</script>

<%@ include file="../footer.jsp" %>