<%@ include file="../header.jsp" %>

<div id="user" class="section">

    <div class="sectioncontent">
        <h2>User Profile (${user})</h2>

        <br>

        <div>
            <form method="POST" action="/id/userService/profile/update">
                <div id=listUserProfile>Loading profile...</div>
            </form>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() {
        // Populate User Profile
        if ("${param.error}" == "") {
            populateDivFromService(
                "/id/userService/profile/listAsTable",
                "listUserProfile",
                "Unable to load this user's profile from the Server");
        } else {
            $(document).ajaxStop(function() {
                $(".error").text("${param.error}");
                $("#cancelButton").click(function() {
                    populateDivFromService(
                        "/id/userService/profile/listAsTable",
                        "listUserProfile",
                        "Unable to load this user's profile from the Server");
                });
            });
            populateDivFromService(
                "/id/userService/profile/listEditorAsTable",
                "listUserProfile",
                "Unable to load this user's profile editor from the Server")
        }
        $(document).ajaxStop(function() {
            $("a", "#profile").click( function() {
                populateDivFromService(
                    "/id/userService/profile/listEditorAsTable",
                    "listUserProfile",
                    "Unable to load this user's profile editor from the Server");
                $(document).ajaxStop(function() {
                    $(".error").text("${param.error}");
                    $("#cancelButton").click(function() {
                        populateDivFromService(
                            "/id/userService/profile/listAsTable",
                            "listUserProfile",
                            "Unable to load this user's profile from the Server");
                    });
                });
            });

        });
    })
</script>

<%@ include file="../footer.jsp" %>