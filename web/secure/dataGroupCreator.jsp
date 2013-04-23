<%@ include file="../header.jsp" %>

<div id="doiMinter" class="section">
    <h2>Data Group Creator</h2>

    <div class="sectioncontent">

        Create a dataset-level identifier that is not associated with any elements. You can associate elements to this dataset
        later in the "Creator" tab. View the <a href="http://code.google.com/p/bcid">BCID codesite</a> for more information.
        <ul>
            <li><b>Title*</b> is required.
            <li><b>Concept*</b> is required.  Each "dataset" can have only one concept.
            <li><b>Target URL</b> is a place where this can resolve to.
            <li><b>DOI</b> indicates a DOI that this dataset belongs to
        </ul>


        <form method="POST" id="dataGroupForm">
            <input type=hidden name=resourceTypes id=resourceTypes value="Dataset">
            <table>
                 <tr>
                    <td align=right>Title*</td>
                    <td><input id=title name=title type=textbox size="40"></td>
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
                    <td><input id=doi name=doi type=textbox size="40"></td>
                </tr>
                <tr>
                    <td align=right>Maintain local IDs</td>
                    <td><input type=checkbox id=suffixPassThrough name=suffixPassThrough checked=yes></td>
                </tr>
                <tr>
                    <td colspan=2>
                    <input type="hidden" name="username" value="<%= request.getRemoteUser() %>" >
                    <input type="button" value="Submit" onclick="dataGroupCreatorSubmit();"/>
                    </td>
                 </tr>
            </table>
        </form>

        <div id="dataGroupCreatorResults" style="overflow:auto;"></div>

    </div>
</div>

<script>
    window.onload = populateSelect("resourceTypesMinusDataset");
</script>

<%@ include file="../footer.jsp" %>
