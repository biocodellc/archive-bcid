<%@ include file="../header.jsp" %>

<div id="doiMinter" class="section">

    <div class="sectioncontent">
        <h2>Biocode Commons Project Creator - DRAFT FORM</h2>

        Create a project holding the core concepts of material sample, event, and information artifact.  Together, these concepts form
        the basis of a Biocode Commons Project.  Each of the core concepts defined per project will be assigned a BCID.  In addition,
        projects can define a DOI, abstract, and a <a href="http://biovalidator.sourceforge.net/">bioValidator</a> XML Validation file.

        <ul>
            <li><b>Project Code*</b> A 4-6 letter code for your project.</li>
            <li><b>Abstract</b> is an abstract for this project.</li>
            <li><b>Material Sample BCID</b> is the BCID </li>

        </ul>

        <div style='float:left;'>
        <form method="POST" id="projectForm">
            <table>
                <tr><td colspan=2><b>Create a New Project</b></td></tr>

                 <tr>
                    <td align=right>FIMS Installation*</td>
                    <td>
                        <select name=fims_id id=fims_id class="">
                            <option value='0'>TEST LIST</option>
                            <option value='1'>Moorea Biocode DB</option>
                            <option value='2'>Smithsonian LAB DB</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td align=right>Project Name*</td>
                    <td><input id=projectname name=projectname type=textbox size="40"></td>
                </tr>
                <tr>
                    <td align=right>Abstract</td>
                    <td><textarea name=abstract id=abstract cols="40" rows="10"></textarea></td>
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

        <div id="projectCreatorResults" style="overflow:auto;"></div>

    </div>
</div>

<script>
    /* use populateselect for available FIMS
    window.onload = populateSelect("resourceTypesMinusDataset");*/
</script>

<%@ include file="../footer.jsp" %>
