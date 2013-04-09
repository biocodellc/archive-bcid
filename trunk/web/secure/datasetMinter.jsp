<%@ include file="../header.jsp" %>

<div id="doiMinter" class="section">
    <h2>Dataset Minter</h2>

    <div class="sectioncontent">

        Create a dataset-level identifier that is not associated with any elements. You can associate elements to this dataset
        later in the "Creator" tab. View the <a href="http://code.google.com/p/bcid">BCID codesite</a> for more information.
        <ul>
            <li><b>Title*</b> is required.
            <li><b>Concept*</b> is required.  Each "dataset" can have only one concept.
            <li><b>Target URL</b> is a place where this can resolve to.
            <li><b>DOI</b> indicates a DOI that this dataset belongs to
        </ul>


        <form method="post" action="/bcid/rest/datasetService">
            <input type=hidden name=resourceTypes id=resourceTypes value="Dataset">
            <table>
                 <tr>
                    <td align=right>Title*</td>
                    <td><input id=title type=textbox size="40"></td>
                </tr>
                 <tr>
                        <td align=right><a href='/bcid/concepts.jsp'>Concept*</a></td>
                        <td><select name=resourceTypesMinusDataset id=resourceTypesMinusDataset class=""></select></td>
                    </tr>
                <tr>
                    <td align=right>Target URL</td>
                    <td><input id=webaddress type=textbox size="40"></td>
                </tr>

                <tr>
                    <td align=right>DOI</td>
                    <td><input id=doi type=textbox size="40"></td>
                </tr>
                <tr>
                    <td colspan=2>
                    <input type="hidden" name="username" value="<%= request.getRemoteUser() %>" >
                    <input type="submit"/>
                    </td>
                 </tr>
            </table>
        </form>
    </div>
</div>

<script>
    window.onload = populateSelect("resourceTypesMinusDataset");
</script>

<%@ include file="../footer.jsp" %>
