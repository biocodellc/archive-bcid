<%@ include file="header.jsp" %>

<div id="resolver" class="section">
    <h2>Resolver</h2>

    <div class="sectioncontent">
        Resolves Biocode Commons Identifiers and EZIDs (e.g. ark:/87286/C2)
        <p></p>
        <input type=textbox name="identifier" id="identifier" style="width:100px;">&nbsp;Identifier
        <br><input type="button" onclick="resolverResults('resolverResults');" name="submit" value="submit" />
    </div>

    <div id="resolverResults" style="overflow:auto;">
    </div>
</div>


<%@ include file="footer.jsp" %>


