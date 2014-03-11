<%@ include file="../header.jsp" %>

<div id="user" class="section">

    <div class="sectioncontent">

        <div>
            <div id=listUserBCIDsAsTable >Loading groups...</div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() {
        populateDivFromService(
            "/id/groupService/listUserBCIDsAsTable",
            "listUserBCIDsAsTable",
            "Unable to load this user's BCIDs from Server");
    });
</script>

<%@ include file="../footer.jsp" %>