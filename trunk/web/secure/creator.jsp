<%@ include file="../header.jsp" %>


<div id="uuidIDs" class="section">

    <h2>Creator</h2>

    <div class="sectioncontent">

        Paste in your Local Identifiers or UUIDs to create BCIDs.
        If you have loaded groups or datasets with this account previously, you can choose an existing group or dataset
        to update a set of existing identifiers.  The default action is to create a new group for this set of identifiers.
        If you elect to NOT maintain local IDs, a short identifier string will be created for you, which you
        can then join back into your database. For more information on what is happening here, visit the
        <a href="http://code.google.com/p/bcid">bcid codesite</a>.

        <form id="localIDMinterForm" action="/bcid/api/bcidService" method="POST">
            <table>
               <tr><td>
                <table style='border-width: 0px 0px 0px 0px !important;'>
                    <tr>
                        <td align=right>Group</td>
                        <td><select name=datasetList id=datasetList class=""></td>
                    </tr>
                    <tr>
                        <td align=right>Concept</td>
                        <td><select name=resourceTypesMinusDataset id=resourceTypesMinusDataset class=""></select></td>
                    </tr>
                    <tr>
                        <td align=right>Maintain local IDs</td>
                        <td align=left><input type=checkbox name=suffixPassThrough checked=yes> </td>
                    </tr>
                </table>
                </td></tr>

                <tr>
                    <td>
                        <textarea name="data" cols="80" rows="10">LocalIdentifier&lt;tab&gt;TargetURL</textarea>
                    </td>
                </tr>
                <tr>
                    <td>
                        <input type="Submit" name="Submit" value="Submit"/>
                    </td>
                </tr>
            </table>
            <input type="hidden" name="username" value="<%= request.getRemoteUser() %>" >
        </form>
    </div>
</div>

<script>
    window.onload = populateSelect("datasetList");
    window.onload = populateSelect("resourceTypesMinusDataset");
</script>

<%@ include file="../footer.jsp" %>
