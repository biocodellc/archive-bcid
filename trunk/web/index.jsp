<%@ include file="header.jsp" %>

<div id="resolver" class="section">
    <h2>Resolver</h2>

    <div class="sectioncontent">
        Lookup Biocode Commons Identifiers.  BCIDs implement a <a href="http://biscicol.org/terms/index.html#suffixPassthrough">suffixPassthrough</a>
        feature to resolve group level identifiers and any suffix attached to it.  For example, if you view the metadata for ark:/21547/R2_MBIO56 you
        will find a link to the Identifier Resolver, which will take you to the home for this particular sample. You can also
        link directly to BCIDs using a link similar to the following: <a href="http://biscicol.org/bcid/ark:/21547/R2_MBIO56">http://biscicol.org/bcid/ark:/21547/R2_MBIO56</a>.

        <p>
        All Biocode Commons Identifier Groups are registered with <a href="http://http://n2t.net/ezid">EZID</a>.  For
        more information on BCIDs view our <a href="https://code.google.com/p/bcid/">help</a> page.

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
                        onkeypress="if(event.keyCode==13) {resolverResults(); return false;}" />  (e.g. ark:/21547/R2_MBIO56)
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


<script>
    /* parse input parameter -- ARKS must be minimum length of 12 characters*/
    var a = '<%=request.getParameter("id")%>';
    if (a.length > 12) {
        $("#identifier").val(a);
        resolverResults();
    }
</script>
<%@ include file="footer.jsp" %>


