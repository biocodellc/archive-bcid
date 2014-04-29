<%@ include file="../header.jsp" %>

<div id="user" class="section">

    <div class="sectioncontent">
        <h2>User Profile (${user})</h2>

        <br>

        <div>
            <div id=listUserProfile>Loading profile...</div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() {
        // Populate User Profile
        if ("${param.error}" == "") {
            var jqxhr = populateDivFromService(
                "/id/userService/profile/listAsTable",
                "listUserProfile",
                "Unable to load this user's profile from the Server");
            loadingDialog(jqxhr);
        } else {
            $(document).ajaxStop(function() {
                $(".error").text("${param.error}");
                $("#cancelButton").click(function() {
                    var jqxhr = populateDivFromService(
                        "/id/userService/profile/listAsTable",
                        "listUserProfile",
                        "Unable to load this user's profile from the Server");
                    loadingDialog(jqxhr);
                });
                $("#profile_submit").click(function() {
                    if ($("input.pwcheck").val().length > 0 && $(".label", "#pwindicator").text() == "weak") {
                        $(".error").html("password too weak");
                    } else {
                        $("form").submit();
                    }
                });
            });
            var jqxhr = populateDivFromService(
                "/id/userService/profile/listEditorAsTable",
                "listUserProfile",
                "Unable to load this user's profile editor from the Server");
            loadingDialog(jqxhr);
        }
        $(document).ajaxStop(function() {
            $("a", "#profile").click( function() {
                var jqxhr = populateDivFromService(
                    "/id/userService/profile/listEditorAsTable",
                    "listUserProfile",
                    "Unable to load this user's profile editor from the Server");
                loadingDialog(jqxhr);
                $(document).ajaxStop(function() {
                    $(".error").text("${param.error}");
                    $("#cancelButton").click(function() {
                        var jqxhr = populateDivFromService(
                            "/id/userService/profile/listAsTable",
                            "listUserProfile",
                            "Unable to load this user's profile from the Server");
                        loadingDialog(jqxhr);
                    });
                    $("#profile_submit").click(function() {
                        if ($("input.pwcheck").val().length > 0 && $(".label", "#pwindicator").text() == "weak") {
                            $(".error").html("password too weak");
                        } else {
                            $("form").submit();
                        }
                    });
                });
            });

        });
    })
</script>

<%@ include file="../footer.jsp" %>