<%@ include file="../header.jsp" %>

<div id="user" class="section">

    <div class="sectioncontent">
        <h2>Manage BCIDs (${user})</h2>

        <div>
            <div id=listUserBCIDsAsTable >Loading groups...</div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() {
        populateBCIDPage();
    });
</script>

<%@ include file="../footer.jsp" %>