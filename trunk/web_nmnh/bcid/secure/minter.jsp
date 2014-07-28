<%@ include file="../header.jsp" %>

<div id="batchMinter" class="section">
    <h2>Minter</h2>

    <div class="sectioncontent">
        <b>This feature is not yet active</b>
        <p>Create globally unique identifiers to apply to your data elements. You must choose a common concept that these
        identifiers share.  There is a limit of 1000 identifiers per request through the web-interface.  If you require
        more please ask.  View <a href="http://code.google.com/p/biscicol/wiki/Identifiers">help</a> for more
        information.

        <p>
        <!--
        <form id='batchMinterForm' method="post" action="">
        <table>
            <tr>
                <td align=right>
                    # bcids to mint
                </td>
                <td>
                    <input type=textbox size="20">
                </td>
            </tr>
            <tr>
                <td align=right>Concept</td>
                <td><select name=resourceTypesMinusDataset id=resourceTypesMinusDataset class=""></select></td>
            </tr>
            <tr>
                <td colspan=2>
                <input type="button" value="send" onclick="send('batchMinterForm');"/>
                </td>
            </tr>
        </table>
        </form>
        -->
    </div>
</div>

<script>
    window.onload = populateSelect("resourceTypesMinusDataset");
</script>

<%@ include file="../footer.jsp" %>



