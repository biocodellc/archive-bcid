<%@ include file="../header.jsp" %>

<div id="doiMinter" class="section">

    <div class="sectioncontent">
        <h2>Project Creator</h2>

        Currently, project creation is limited to the REST/application programming interface.  Please check with the system manager to have a project created for you.

        <!--
        <ul>
            <li><b>Project Code*</b> A 4-6 letter code for your project.</li>
            <li><b>Project Title*</b> A brief title for this project.</li>
            <li><b>Resolver Web Address</b> The web root for resolving child BCIDs (e.g. http://example.com/ will construct http://example.com/Agent/, http://example.com/Occurrence/, etc..)</li>
            <li><b>Abstract</b> is an abstract for this project.</li>
            <li><b>Validation URL</b> is a URL pointer to the Validation XML file as specified by <a href="http://code.google.com/p/biocode-fims/">biocode-fims</a></li>
        </ul>

        <form method="POST" id="projectForm">
            <table>
                <tr><td colspan=2><b>Create a New Project</b></td></tr>

                <tr>
                    <td align=right>Project Code*</td>
                    <td><input id=project_code name=project_code type=textbox size="40"></td>
                </tr>
                <tr>
                    <td align=right>Project Title*</td>
                    <td><input id=project_title name=project_title type=textbox size="40"></td>
                </tr>
                <tr>
                    <td align=right>Resolver Web Address</td>
                    <td><input id=resolverWebAddress name=resolverWebAddress type=textbox size="40"></td>
                </tr>
                <tr>
                    <td align=right>Abstract</td>
                    <td><textarea name=abstract id=abstract cols="100" rows="10"></textarea></td>
                </tr>
                <tr>
                    <td align=right>Validation URL</td>
                    <td><input name=biovalidator_Validation_xml id=bioValidator_validation_xml type=textbox size="40"></td>
                </tr>
                <tr>
                    <td colspan=2>
                    <input type="hidden" name="username" value="<%= request.getRemoteUser() %>" >
                    <input type="button" value="Submit" onclick="projectCreatorSubmit();"/>
                    </td>
                 </tr>
            </table>
        </form>
        <div id="projectCreatorResults" class="sectioncontent-results"></div>
        -->
    </div>
</div>

<%@ include file="../footer.jsp" %>
