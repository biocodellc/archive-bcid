<%@ include file="../header.jsp" %>

<div id="doiMinter" class="section">

    <div class="sectioncontent">
        <h2>BCID Creator (${user})</h2>

        Create a group-level identifier that acts as a container for an unlimited number of user-specified elements.

        View the <a href="http://code.google.com/p/bcid">BCID codesite</a> for more information.
        <ul>
            <li><b>Title*</b> is required.
            <li><b>Concept*</b> is required.  Each group level identifier can only represent one type of concept.
            <li><b>Target URL</b> is a place where requests for this identifier and any suffixes will resolve to.
            <li><b>DOI</b> indicates a DOI that this dataset belongs to
            <li><b>Rights</b> All BCIDS fall under <a href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0</a>
            <li><b>Follow Suffixes</b> Check this box if you intend to append suffixes to this group for resolution.
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
                    <td><input id=webaddress name=webaddress type=textbox size="40"></td>
                </tr>

                <tr>
                    <td align=right>DOI</td>
                    <td><input id=doi name=doi type=textbox size="40"></td>
                </tr>

                <tr>
                    <td align=right>Rights</td>
                    <td><a href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0</a></td>
                </tr>

                <tr>
                    <td align=xright>Follow Suffixes</td>
                    <td><input type=checkbox id=suffixPassThrough name=suffixPassThrough checked=yes></td>
                </tr>

                <tr>
                    <td colspan=2>
                    <input type="hidden" name="username" value="${user}" >
                    <input type="button" value="Submit" onclick="dataGroupCreatorSubmit();"/>
                    </td>
                 </tr>
            </table>
        </form>

        <div id="dataGroupCreatorResults" class="sectioncontent-results"></div>

    </div>
</div>

<script>
    window.onload = populateSelect("resourceTypesMinusDataset");
</script>

<%@ include file="../footer.jsp" %>
