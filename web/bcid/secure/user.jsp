<%@ include file="../header.jsp" %>

<div id="user" class="section">

    <div class="sectioncontent">
        <h2>${user}s Profile</h2>

        <br>

        <div>
            <div id=listUserProfile>Loading profile...</div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() {
        // Populate User Profile
        populateDivFromService(
            "/id/userService/profile/listAsTable",
            "listUserProfile",
            "Unable to load this user's profile from the Server");
    })
</script>

<%@ include file="../footer.jsp" %>