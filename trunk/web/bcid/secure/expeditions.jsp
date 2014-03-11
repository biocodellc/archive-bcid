<%@ include file="../header.jsp" %>

<div id="user" class="section">

    <div class="sectioncontent">

        <div>
            <div id=listUserExpeditionsAsTable >Loading expeditions...</div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() {
        populateDivFromService(
            "/id/groupService/listUserExpeditionsAsTable",
            "listUserExpeditionsAsTable",
            "Unable to load this user's expeditions from Server");
    });
</script>

<%@ include file="../footer.jsp" %>