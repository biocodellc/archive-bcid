<%@ include file="../header.jsp" %>

<div id="uuidIDs" class="section">

    <h2>Creator</h2>

    <div class="sectioncontent">
        Paste in your Local Identifiers or UUIDs to create BCIDs for individual elements.  This is useful if you need
        to individually resolve identifiers to unique locations, otherwise, we reccomend you use the Data Group creator.
        If you have loaded datasets with this account previously, you can choose an existing dataset
        to update a set of existing identifiers.  The default action is to create a new dataset for this set of identifiers.
        If you elect to create a new group, the following information applies.
        <ul>
            <li><b>Title*</b> is required.
            <li><b>Concept*</b> is required.  Each group level identifier can only represent one type of concept.
            <li><b>Target URL</b> is a place where requests for this identifier and any suffixes will resolve to.
            <li><b>DOI</b> indicates a DOI that this dataset belongs to
            <li><b>Follow Suffixes</b> Check this box if you intend to append suffixes to this group for resolution.
        </ul>
        For more information on what is happening here, visit the
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
                        <td align=right>Follow Suffixes</td>
                        <td align=left><div id=suffixPassThroughDiv></div></td>
                    </tr>
                </table>
                </td></tr>

                <tr>
                    <td>
                        You can paste in a column of local identifiers below, or optionally a 2nd column,
                        <br>with a "|" delimiter of webaddresses to resolve the identifiers to.
                        <br>E.g. ID1|http://webaddress/resolver/ID1
                        <br>    <textarea name="data" cols="80" rows="10"></textarea>
                    </td>
                </tr>
                <tr>
                    <td>
                        <input type="button" value="Submit" onclick="creatorSubmit();"/>
                    </td>
                </tr>
            </table>
            <input type="hidden" name="username" value="${user}" >
        </form>

        <div id="creatorResults" class="sectioncontent-results"></div>

    </div>
</div>

<script>
    window.onload = populateSelect("datasetList");
    window.onload = creatorDefaults();
</script>

<%@ include file="../footer.jsp" %>
