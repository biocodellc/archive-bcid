<%@ include file="../header.jsp" %>

<div id="doiMinter" class="section">
    <h2>Dataset IDs</h2>

    <div class="sectioncontent">

        Create a dataset-level identifier that is not associated with any elements. You can associate elements to this dataset
        later in the "Creator" tab. Title is required.  Target URL is a place where this can resolve to.  Indicating a DOI joins
        this dataset identifier to an existing DOI. View the <a href="http://code.google.com/p/bcid">bcid codesite</a> for more information.

        <form method="post" action="/bcid/api/datasetService">
            <input type=hidden name=resourceTypes id=resourceTypes value="Dataset">
            <table>
                 <tr>
                    <td align=right>Title*</td>
                    <td><input id=title type=textbox size="40"></td>
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

<!--
<script>

</script>
-->
<%@ include file="../footer.jsp" %>
