<%@ include file="../header.jsp" %>

<div id="user" class="section">

    <div class="sectioncontent">
        <h2>User Tools</h2>

        <p>
        List of Data Groups accessible to <%= request.getRemoteUser() %>
        <p>
        (<a href='/bcid/secure/dataGroupCreator.jsp'>+</a>) Add a Data Group
        <p>
        <div name=listTable id=listTable style="overflow:auto;">Loading groups</div>

    </div>
</div>

<script>
    window.onload = populateDataGroupTable('listTable');
</script>


<%@ include file="../footer.jsp" %>