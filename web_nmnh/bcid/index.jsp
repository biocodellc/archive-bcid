<%@ include file="header.jsp" %>

<div id="resolver" class="section">

        <h2>Lookup</h2>

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
                        onkeypress="if(event.keyCode==13) {resolverResults(); return false;}" />  (e.g. ark:/21547/R2MBIO56)
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

        <p><h2>About</h2><br>

        BCIDs are free, persistent, scalable, resolvable identifiers for all biological and biodiversity samples, sub-samples, and processes.
        They are an extension of the <a href="http://ezid.cdlib.org">California Digital Library EZID solution</a> and fall under
        the <a href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 License</a>.  Since they are
        self-describing and require attribution, the only requirement for their use and persistence is that
        they maintain their form in any consuming application.

        <p>

        BCIDs require a resolution service to deliver content upon request.
        The BCID resolution service implements
        <a href="http://biscicol.org/terms/index.html#suffixPassthrough">suffixPassthrough</a> for all identifiers, meaning
        that it can recognize identifiers it has never seen before by appending local-specific identifiers as suffixes to
        group-level identifiers.
         A metadata service provides metadata about the identifier itself (in HTML format if you are human or RDF/XML format
        if you are machine). The following table lists sample BCID and EZID resolution and metadata services for the
        identifier ark:/21547/R2MBIO56 :

         <table class="subtable" border=0>
            <tr><td>URL</td><td>Service</td><td>Description</td></tr>
            <tr>
                <td><a href="http://biscicol.org/id/ark:/21547/R2MBIO56">http://biscicol.org/id/ark:/21547/R2MBIO56</a></td>
                <td>BCID</td>
                <td>Resolution service for a group plus suffix identifier</td>
            </tr>
             <tr>
                <td><a href="http://biscicol.org/id/metadata/ark:/21547/R2MBIO56">http://biscicol.org/id/metadata/ark:/21547/R2MBIO56</a></td>
                <td>BCID</td>
                <td>Metadata service for a group plus suffix</td>
            </tr>
            <tr>
                <td><a href="http://n2t.net/ark:/21547/R2">http://n2t.net/ark:/21547/R2</a></td>
                <td>EZID</td>
                <td>Resolution service for a group identifier</td>
            </tr>
            <tr>
                <td>http://n2t.net/ark:/21547/R2MBIO56</td>
                <td>EZID</td>
                <td>Resolution service for a group plus suffix identifier, not active yet!</td>
            </tr>
            <tr>
                <td><a href="http://ezid.cdlib.org/id/ark:/21547/R2">http://n2t.net/ezid/id/ark:/21547/R2</a></td>
                <td>EZID</td>
                <td>Metadata service for a group identifier</td>
            </tr>
        </table>

    </div>
</div>

<script>
    /* parse input parameter -- ARKS must be minimum length of 12 characters*/
    var a = '<%=request.getParameter("id")%>';
    if (a.length > 12) {
        $("#identifier").val(a);
        resolverResults();
    }
</script>
<%@ include file="footer.jsp" %>
