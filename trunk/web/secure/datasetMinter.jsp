<%@ include file="../header.jsp" %>

<div id="doiMinter" class="section">
    <h2>Dataset Identifier</h2>

    <div class="sectioncontent">
        Create an identifier for your dataset that will be registered through EZID and DataCite.
        <ul>
            <li><b>*Concept</b> is mandatory and refers to the data elements that this dataset contains.  For instance,
            for a dataset of specimens (or, Darwin Core "Occurrences"), you would choose Occurrence.  Datasets typically should
            contain sets of similiar concepts.  Hence, if you also had Collecting Event data, you would make a new
            dataset identifier with the concept of "Event".</li>
            <li><b>Target URL</b> will be used by the resolver to redirect incoming requests to this particular URL.</li>
            <li><b>Title</b> the title for this dataset.
            <li><b>DOI</b> is the Digital Object Identifier for this dataset, if applicable.
        </ul>
        View <a
            href="http://code.google.com/p/biscicol/wiki/Identifiers">help</a> for more information.
        <p>

        <form method="post" action="/bcid/api/datasetService">
            <table>
                <tr>
                    <td align=right><select name=resourceTypes id=resourceTypes class=""></select></td>
                    <td>*Concept</td>
                </tr>
                <tr>
                    <td align=right><input id=webaddress type=textbox></td>
                    <td>Target URL</td>
                </tr>
                <tr>
                    <td align=right><input id=title type=textbox></td>
                    <td>Title</td>
                </tr>
                <tr>
                    <td align=right><input id=doi type=textbox></td>
                    <td>DOI</td>
                </tr>
            </table>
            <input type="hidden" name="username" value="<%= request.getRemoteUser() %>" >
            <input type="submit"/>
        </form>
    </div>
</div>

<script>
    window.onload = populateSelect("resourceTypes");
</script>

<%@ include file="../footer.jsp" %>
