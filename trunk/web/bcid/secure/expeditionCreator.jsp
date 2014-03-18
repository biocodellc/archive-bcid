<%@ include file="../header.jsp" %>

<div id="doiMinter" class="section">

    <div class="sectioncontent">
        <h2>Expedition Creator</h2>

        Currently, expedition creation is limited to the REST/application programming interface.  Please check with the system manager to have a expedition created for you.

        <!--
        <ul>
            <li><b>Expedition Code*</b> A 4-6 letter code for your expedition.</li>
            <li><b>Expedition Title*</b> A brief title for this expedition.</li>
            <li><b>Resolver Web Address</b> The web root for resolving child BCIDs (e.g. http://example.com/ will construct http://example.com/Agent/, http://example.com/Occurrence/, etc..)</li>
            <li><b>Validation URL</b> is a URL pointer to the Validation XML file as specified by <a href="http://code.google.com/p/biocode-fims/">biocode-fims</a></li>
        </ul>

        <form method="POST" id="expeditionForm">
            <table>
                <tr><td colspan=2><b>Create a New Expedition</b></td></tr>

                <tr>
                    <td align=right>Expedition Code*</td>
                    <td><input id=expedition_code name=expedition_code type=textbox size="40"></td>
                </tr>
                <tr>
                    <td align=right>Expedition Title*</td>
                    <td><input id=expedition_title name=expedition_title type=textbox size="40"></td>
                </tr>
                <tr>
                    <td align=right>Resolver Web Address</td>
                    <td><input id=resolverWebAddress name=resolverWebAddress type=textbox size="40"></td>
                </tr>
                <tr>
                    <td align=right>Validation URL</td>
                    <td><input name=biovalidator_Validation_xml id=bioValidator_validation_xml type=textbox size="40"></td>
                </tr>
                <tr>
                    <td colspan=2>
                    <input type="hidden" name="username" value="${user}" >
                    <input type="button" value="Submit" onclick="expeditionCreatorSubmit();"/>
                    </td>
                 </tr>
            </table>
        </form>
        <div id="expeditionCreatorResults" class="sectioncontent-results"></div>
        -->
    </div>
</div>

<%@ include file="../footer.jsp" %>
