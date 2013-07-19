<%@ include file="../header.jsp" %>

<div id="doiMinter" class="section">

    <div class="sectioncontent">
        <h2>Project Creator - SERVICE NOT CURRENTLY AVAILABLE</h2>

        Create a project holding the core concepts of material sample, event, and information artifact.  Together, these concepts form
        the basis of a Biocode Commons Project and are created automatically when the project itself is created.
        Each of the core concepts defined per project will be assigned a group-level BCID.  All suffixes assigned by users
        for these identifiers will be appended to the group-level identifiers using suffixPassthrough.  In addition,
        projects can optionally define an abstract and a <a href="http://biovalidator.sourceforge.net/">bioValidator</a> XML Validation file.

        <ul>
            <li><b>Project Code*</b> A 4-6 letter code for your project.</li>
            <!--
            <li><b>Material Sample BCID</b> is the identifier for all <a href="http://purl.obolibrary.org/obo/OBI_0100051">Material Samples</a> in this project</li>
            <li><b>Event BCID</b> is the identifier for all <a href="http://purl.org/dc/dcmitype/Event">Events</a> in this project</li>
            <li><b>Information Artifact BCID</b> is the identifier for all <a href="http://purl.obolibrary.org/obo/IAO_0000030">Information Artifacts</a> in this project</li>
            -->
            <li><b>Abstract</b> is an abstract for this project.</li>
            <li><b>Validation XML</b> is the XML used to validate data loaded for this project, as specified by <a href="http://biovalidator.sourceforge.net/">bioValidator</a></li>
        </ul>

        <div style='float:left;'>
        <form method="POST" id="projectForm">
            <table>
                <tr><td colspan=2><b>Create a New Project</b></td></tr>

                <tr>
                    <td align=right>Project Code*</td>
                    <td><input id=project_code name=project_code type=textbox size="40"></td>
                </tr>
                <tr>
                    <td align=right>Abstract</td>
                    <td><textarea name=abstract id=abstract cols="100" rows="10"></textarea></td>
                </tr>
                <tr>
                    <td align=right>Validation XML</td>
                    <td><textarea name=biovalidator_Validation_xml id=bioValidator_validation_xml cols="100" rows="10"></textarea></td>
                </tr>
                <tr>
                    <td colspan=2>
                    <input type="hidden" name="username" value="<%= request.getRemoteUser() %>" >
                    <input type="button" value="Submit" onclick="projectCreatorSubmit();"/>
                    </td>
                 </tr>
            </table>
        </form>
        </div>

        <!--
        <div style='float:left;width:10px;'>&nbsp;&nbsp;&nbsp;&nbsp;</div>
        <div style='float:left;'>
        <table>
            <tr><td colspan=2><b>{IMPLEMENTATION NAME}</b></td></tr>
            <tr>
                <td>abstract</td><td>ABSTRACT GOES HERE</td>
            </tr>
            <tr>
                <td>xml_validation_url</td><td>xml_validation_url</td>
            </tr>
            <tr>
                <td>db_location_url</td><td>db_location_url</td>
            </tr>
            <tr>
                <td>Identifier</td><td>DOI/ARK for Implementation</td>
            </tr>
        </table>
        </div>
        -->

        <div id="projectCreatorResults" style="overflow:auto;"></div>

    </div>
</div>

<script>
    /* use populateselect for available FIMS
    window.onload = populateSelect("resourceTypesMinusDataset");*/
</script>

<%@ include file="../footer.jsp" %>
