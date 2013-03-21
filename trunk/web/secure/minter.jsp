<%@ include file="../header.jsp" %>

<div id="batchMinter" class="section">
    <h2>Create Globally Unique Identifiers (BCIDs)</h2>

    <div class="sectioncontent">
        Create globally unique identifiers to apply to your data elements. Run them through the Triplifier to
        turn them into EZIDs. View <a href="http://code.google.com/p/biscicol/wiki/Identifiers">help</a> for more
        information.

        <p>

        <form id='batchMinterForm' method="post" action="">
            <input type=textbox>&nbsp;Number of GUIDs to create
            <br>
            <input type="button" value="send" onclick="send('batchMinterForm');"/>
        </form>
    </div>
</div>

<%@ include file="../footer.jsp" %>



