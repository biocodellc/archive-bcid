<%@ include file="../header.jsp" %>

<div id="bcidEditor" class="section">

    <div class="sectioncontent">
        <h2>Edit <%= request.getParameter("ark") %></h2>

        Service to edit the content of an identifier -- NOT CURRENTLY FUNCTIONAL.

        <form method="POST" id="dataGroupEditForm">
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
                    <td align=right>Follow Suffixes</td>
                    <td><input type=checkbox id=suffixPassThrough name=suffixPassThrough checked=yes></td>
                </tr>

                <tr>
                    <td colspan=2>
                    <input type="hidden" name="username" value="<%= request.getRemoteUser() %>" >
                    <input type="button" value="Submit" onclick="dataGroupEditorSubmit();"/>
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

