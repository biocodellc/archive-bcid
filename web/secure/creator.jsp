<%@ include file="../header.jsp" %>

<div id="uuidIDs" class="section">

    <h2>Creator</h2>

    <div class="sectioncontent">
        Paste in your Local Identifiers or UUIDs to create BCIDs.
        If you have loaded datasets with this account previously, you can choose an existing dataset
        to update a set of existing identifiers.  The default action is to create a new dataset for this set of identifiers.
        If you elect to NOT maintain local IDs, a short identifier string will be created for you, which you
        can then join back into your database. For more information on what is happening here, visit the
        <a href="http://code.google.com/p/bcid">bcid codesite</a>.

        <form id="localIDMinterForm" method="POST">
            <table>
               <tr><td>
                <table style='border-width: 0px 0px 0px 0px !important;'>
                    <tr>
                        <td align=right>Dataset</td>
                        <td><select name=datasetList id=datasetList class="" onchange='datasetListSelector()'></td>
                    </tr>
                     <tr>
                        <td align=right>Title</td>
                        <td><div id=titleDiv></div></td>
                    </tr>
                    <tr>
                        <td align=right><a href='/bcid/concepts.jsp'>Concept</a></td>
                        <td><div id=resourceTypesMinusDatasetDiv></td>
                    </tr>
                    <tr>
                        <td align=right>doi</td>
                        <td><div id=doiDiv></td>
                    </tr>
                    <tr>
                        <td align=right>Maintain local IDs</td>
                        <td align=left><div id=suffixPassThroughDiv></div></td>
                    </tr>
                </table>
                </td></tr>

                <tr>
                    <td>
                        <textarea name="data" cols="80" rows="10">LocalIdentifier</textarea>
                    </td>
                </tr>
                <tr>
                    <td>
                        <input type="button" value="Submit" onclick="creatorSubmit();"/>
                    </td>
                </tr>
            </table>
            <input type="hidden" name="username" value="<%= request.getRemoteUser() %>" >
        </form>

        <div id="creatorResults" style="overflow:auto;"></div>

    </div>
</div>

<script>
    window.onload = populateSelect("datasetList");
    window.onload = creatorDefaults();
</script>

<%@ include file="../footer.jsp" %>
