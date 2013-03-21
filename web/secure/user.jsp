<%@ include file="../header.jsp" %>

<div id="user" class="section">
    <h2>User Tools</h2>

    <div class="sectioncontent">
        <p>
        List of Datasets accessible to <%= request.getRemoteUser() %> (this part not functional yet):
        <p>
        (<a href='/bcid/secure/doiMinter.jsp'>+</a>) Add a dataset

    </div>
</div>

<%@ include file="../footer.jsp" %>