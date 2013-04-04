<%@ include file="../header.jsp" %>


<div id="uuidIDs" class="section">

    <h2>Turn Local ID's or UUIDs into Identifiers</h2>

    <div class="sectioncontent">
        Paste in your Local Identifiers or UUIDs to create BCIDs.
         <a href="http://code.google.com/p/biscicol/wiki/Identifiers">more information</a>

        <br>

        <form id="localIDMinterForm" action="/bcid/api/bcidService" method="POST">
            <table>
                <tr>
                    <td align=right>
                        <select name=datasetList id=datasetList class="">
                    </td>
                    <td>Dataset</td>

                    <td>Apply these identifiers to an existing dataset (optional. E.g. ark:/87286/A2)</td>
                </tr>
                <tr>
                    <td colspan=2><input type=checkbox name=suffixPassThrough> Maintain local IDs in the identifier suffix</td>
                </tr>
                <tr>
                    <td colspan=2><textarea name="data" cols="80"
                                            rows="10">LocalIdentifier&lt;tab&gt;TargetURL</textarea></td>
                </tr>

                <tr>
                    <td colspan=2>
                        <input type="button" value="validate" onclick="validate()"/>&nbsp;&nbsp;
                        <input type="submit"/>
                    </td>
                </tr>
            </table>
            <input type="hidden" name="username" value="<%= request.getRemoteUser() %>" >
        </form>
    </div>
</div>

<script>
    window.onload = populateSelect("datasetList");
</script>

<%@ include file="../footer.jsp" %>
