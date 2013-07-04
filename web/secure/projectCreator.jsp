<%@ include file="../header.jsp" %>

<div id="doiMinter" class="section">
    <h2>Project Creator</h2>

    <div class="sectioncontent">
        <h2>DRAFT FORM -- Presented here to show proposed functionality only, not operational</h2>
        Create a Project that defines sets of Data Groups for use in a Field Information Management System. You will
        automatically be assigned ARKs for all Samples, Events, and Information Artifacts.

        <ul>
            <li><b>FIMS Installation*</b> defines the particular FIMS installation you wish to associate this project
            with.  You will only be shown FIMS systems to which you have been granted access.  You must ask the system administrator
            for access to the FIMS.
            <li><b>Project Code*</b> A 4-6 letter code for your project.  It must be unique across all FIMS instances.
            <li><b>Abstract</b> is an abstract for this project.
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
                            <option value='1'>Biocode</option>
                            <option value='2'>Smithsonian</option>
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
            <tr><td colspan=2><b>{FIMS_NAME} FIMS Details</b></td></tr>
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
                <td>fims ARK</td><td>ARK for FIMS goes here</td>
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
