<%@ include file="../header.jsp" %>

<div class="section">
    <div class="sectioncontent"></div>
</div>

<script>
    $(document).ready(function() {
        populateProjectPage("${user}");
    });
</script>

<%@ include file="../footer.jsp" %>