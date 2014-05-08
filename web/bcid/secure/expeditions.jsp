<%@ include file="../header.jsp" %>

<div class="section">
    <div class="sectioncontent">Loading Datasets...</div>
</div>

<script>
    $(document).ready(function() {
        populateExpeditionPage("${user}");
    });
</script>

<%@ include file="../footer.jsp" %>