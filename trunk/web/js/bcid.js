// Control functionality when a datasetList is activated in the creator page
// if it is not option 0, then need to look up values from server to fill
// in title and concept.
function datasetListSelector() {

    // Set values when the user chooses a particular dataset
    if ($("#datasetList").val() != 0) {
        // Construct the URL
        var url = "/bcid/rest/datasetService/metadata/" + $("#datasetList").val();
        // Initialize cells
        $("#resourceTypesMinusDatasetDiv").html("");
        $("#suffixPassthroughDiv").html("");
        $("#titleDiv").html("");
        // Get JSON response
        var jqxhr = $.getJSON(url, function() {})
            .done(function(data) {
                var options = '';
                $.each(data[0], function(key, val) {
                    // Assign values from server to JS field names
                    if (key == "what")
                        $("#resourceTypesMinusDatasetDiv").html(val);
                    if (key == "identifiersSuffixPassthrough")
                        $("#suffixPassthroughDiv").html(val);
                    if (key == "title")
                        $("#titleDiv").html(val);
                });
            });
        // Set styles
        var color = "#463E3F";
        $("#titleDiv").css("color",color);
        $("#resourceTypesMinusDatasetDiv").css("color",color);
        $("#suffixPassthroughDiv").css("color",color);

    // Set the Creator Defaults
    } else {
        creatorDefaults();
    }
}

// Set default settings for the Creator Form settings
function creatorDefaults() {
    $("#titleDiv").html("<input id=title type=textbox size=40>");
    $("#resourceTypesMinusDatasetDiv").html("<select name=resourceTypesMinusDataset id=resourceTypesMinusDataset class=''>");
    $("#suffixPassthroughDiv").html("<input type=checkbox id=suffixPassthrough name=suffixPassThrough checked=yes>");

    $("#titleDiv").css("color","black");
    $("#resourceTypesMinusDatasetDiv").css("color","black");
    $("#suffixPassthroughDiv").css("color","black");

    populateSelect("resourceTypesMinusDataset");
}

// Populate a table of data showing resourceTypes
function populateResourceTypes(a) {
    var url = "/bcid/rest/bcidService/resourceTypes";
    var jqxhr = $.ajax(url, function() {})
        .done(function(data) {
           $("#" + a).html(data);
        })
        .fail(function() {
            $("#" + a).html("Unable to load resourceTypes!");
        });
}

// Populate the SELECT box with resourceTypes from the server
function populateSelect(a) {
    // Dataset Service Call
    if (a == "datasetList") {
        var url = "/bcid/rest/datasetService/list";
    // bcid Service Call
    } else {
        var url = "/bcid/rest/bcidService/select/" + a;
    }

    // get JSON from server and loop results
    var jqxhr = $.getJSON(url, function() {})
        .done(function(data) {
            var options = '';
            $.each(data[0], function(key, val) {
                options+='<option value="' + key + '">' +val + '</option>';
            });
            $("#" + a).html(options);
        });
}

// Take the resolver results and populate a table
function resolverResults(target_id) {

    $("#" + target_id).html("<div>Processing request ... </div>");
    var div = "";

    var jqxhr = $.getJSON("/bcid/rest/resolverService/" + $("#identifier").val() , function(data) {
        var count=0;
        $.each(data, function() {
            var tbl_body = "<div style='float:left;margin:20px;'>";
            if (count ==0)
                tbl_body += "BCID Resolution:";
            else
                tbl_body += "EZID Resolution:";
            tbl_body += "<table border=1>";
            $.each(this, function(k , v) {
                tbl_body += "<tr><td>"+k+"</td>" + "<td>"+v+"</td></tr>";
            })
            tbl_body += "</table></div>";
            div += tbl_body;
            count++;
        })
    })
    .done(function() { $("#" + target_id).html(div); })
    .fail(function() { $("#" + target_id).html("<div>Unable to resolve " + $("#identifier").val() + "</div>"); });
    //.always(function() { console.log( "complete" ); });
}