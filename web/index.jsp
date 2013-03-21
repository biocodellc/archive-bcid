<%@ include file="header.jsp" %>

<div id="resolver" class="section">
    <h2>Resolver</h2>

    <div class="sectioncontent">
        Resolves Biocode Commons Identifiers and EZIDs (e.g. ark:/87286/C2)
        <p></p>

        <form action="/bcid/rest/resolverService" method="post">
            <input type=textbox name="identifier" id=identifier>&nbsp;Identifier
            <br><input type="submit"/>
        </form>
    </div>
</div>


<%@ include file="footer.jsp" %>


