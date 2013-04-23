<%@ include file="header.jsp" %>

<div id="resolver" class="section">
    <h2>Resolver</h2>

    <div class="sectioncontent">
        Resolves Biocode Commons Identifiers and EZIDs (e.g. ark:/87286/C2).  The BCID resolver recognizes suffixes attached
        to known datasets, resolving back to the dataset itself.  EZID is slated to implement this service as well soon.

        <form>
        <table border=0>
            <tr>
                <td align=right>Identifier</td>
                <td align=left>
                    <input
                        type=text
                        name="identifier"
                        id="identifier"
                        size="40"
                        onkeypress="if(event.keyCode==13) {resolverResults(); return false;}" />
                </td>
            </tr>
            <tr>
                <td colspan=2>
                    <input
                        type="button"
                        onclick="resolverResults();"
                        name="Submit"
                        value="Submit" />
                </td>
            </tr>
         </table>
         </form>
             <div id="resolverResults" style="overflow:auto;">

    </div>

    </div>
</div>


<%@ include file="footer.jsp" %>


