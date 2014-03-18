<%@ include file="../header.jsp" %>

<div id="user" class="section">

    <div class="sectioncontent">
        <h2>Manage BCIDs (${user})</h2>

        <div>
            <div id=listUserBCIDsAsTable >Loading groups...</div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() {
        populateDivFromService(
            "/id/groupService/listUserBCIDsAsTable",
            "listUserBCIDsAsTable",
            "Unable to load this user's BCIDs from Server");
        $(document).one("ajaxStop", function() {
            $("a.edit").click(function() {
                populateDivFromService(
                    "/id/groupService/dataGroupEditorAsTable?ark=" + this.dataset.ark,
                    "listUserBCIDsAsTable",
                    "Unable to load the BCID editor from Server");
            });
            $(document).one("ajaxStop", function() {
                populateSelect("resourceTypesMinusDataset");
                $("#cancelButton").click(function() {
                    window.location.reload();
                });
                $(document).one("ajaxStop", function() {
                    var options = $('option')
                    $.each(options, function() {
                        if ($('select').data('resource_type') == this.text) {
                            $('select').val(this.value);
                        }
                    });
                });
            });
        });
    });
</script>

<%@ include file="../footer.jsp" %>