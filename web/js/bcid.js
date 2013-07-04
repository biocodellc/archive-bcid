/** Process submit button for Data Group Creator **/
function dataGroupCreatorSubmit() {
    $( "#dataGroupCreatorResults" ).html( "Processing ..." );
    /* Send the data using post */
    var posting = $.post( "/bcid/groupService", $("#dataGroupForm").serialize() );
    results(posting,"#dataGroupCreatorResults");
}

/** Process submit button for Data Group Creator **/
function projectCreatorSubmit() {
    $( "#projectCreatorResults" ).html( "Processing ..." );
    /* Send the data using post */
    // Temporary output
    results($("#projectForm").serialize(),"#projectCreatorResults");
    /*
    var posting = $.post( "/bcid/rest/projectService", $("#projectForm").serialize() );
    results(posting,"#projectCreatorResults");
    */
}

/** Process submit button for Creator **/
function creatorSubmit() {
    $( "#creatorResults" ).html( "Processing ..." );
    /* Send the data using post */
    var posting = $.post( "/bcid/elementService/creator", $("#localIDMinterForm").serialize() );
    results(posting, "#creatorResults");
}

/** Generic way to display results from creator functions, relies on standardized JSON **/
function results(posting, a) {
    // Put the results in a div
    posting.done(function( data ) {
        var content = "<table>";
        content += "<tr><th>Results</th></tr>"
        $.each(data, function(k,v) {
            content += "<tr><td>"+v+"</td></tr>";
        })
        content += "</table>";
        $( a ).html( content );
    });
   posting.fail(function() {
        $( a ).html( "<table><tr><th>System error, unable to perform function!!</th></tr></table>" );
   });
}

// Control functionality when a datasetList is activated in the creator page
// if it is not option 0, then need to look up values from server to fill
// in title and concept.
function datasetListSelector() {

    // Set values when the user chooses a particular dataset
    if ($("#datasetList").val() != 0) {
        // Construct the URL
        var url = "/bcid/groupService/metadata/" + $("#datasetList").val();
        // Initialize cells
        $("#resourceTypesMinusDatasetDiv").html("");
        $("#suffixPassThroughDiv").html("");
        $("#titleDiv").html("");
        $("#doiDiv").html("");

        // Get JSON response
        var jqxhr = $.getJSON(url, function() {})
            .done(function(data) {
                var options = '';
                $.each(data[0], function(keyData,valData) {
                    $.each(valData, function(key, val) {
                        // Assign values from server to JS field names
                        if (key == "what")
                            $("#resourceTypesMinusDatasetDiv").html(val);
                        if (key == "datasetsSuffixPassThrough")
                            $("#suffixPassThroughDiv").html(val);
                        if (key == "title")
                            $("#titleDiv").html(val);
                        if (key == "doi")
                            $("#doiDiv").html(val);
                    });
                });
            });
        // Set styles
        var color = "#463E3F";
        $("#titleDiv").css("color",color);
        $("#resourceTypesMinusDatasetDiv").css("color",color);
        $("#suffixPassThroughDiv").css("color",color);
        $("#doiDiv").css("color",color);

    // Set the Creator Defaults
    } else {
        creatorDefaults();
    }
}

// Set default settings for the Creator Form settings
function creatorDefaults() {
    $("#titleDiv").html("<input id=title name=title type=textbox size=40>");
    $("#resourceTypesMinusDatasetDiv").html("<select name=resourceTypesMinusDataset id=resourceTypesMinusDataset class=''>");
    $("#suffixPassThroughDiv").html("<input type=checkbox id=suffixPassThrough name=suffixPassThrough checked=yes>");
    $("#doiDiv").html("<input type=textbox id=doi name=doi checked=yes>");

    $("#titleDiv").css("color","black");
    $("#resourceTypesMinusDatasetDiv").css("color","black");
    $("#suffixPassThroughDiv").css("color","black");
    $("#doiDiv").css("color","black");

    populateSelect("resourceTypesMinusDataset");
}

// Populate a table of data showing group Elements
function populateDataGroupTable(a)  {
    var url = "/bcid/groupService/listTable";
    var jqxhr = $.ajax(url, function() {})
        .done(function(data) {
           $("#" + a).html(data);
        })
        .fail(function() {
            $("#" + a).html("Unable to load groupService elements!");
        });
}

// Populate a table of data showing resourceTypes
function populateResourceTypes(a) {
    var url = "/bcid/elementService/resourceTypes";
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
        var url = "/bcid/groupService/list";
    // bcid Service Call
    } else {
        var url = "/bcid/elementService/select/" + a;
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
function resolverResults() {
    window.location.replace("/bcid/id/" + $("#identifier").val());
    /*
    $("#resolverResults").html("<div>Processing request ... </div>");
    var div = "";

    var jqxhr = $.getJSON("/bcid/metadata/" + $("#identifier").val() , function(data) {
        $.each(data, function() {
            var tbl_body = "<div style='float:left;margin:20px;'>";
            tbl_body += "<table border=1>";
            // Loop metadata elements
            $.each(this,function(k,v) {
                tbl_body += "<tr><td>"+k+"</td>" + "<td>"+v+"</td></tr>";
            })
            tbl_body += "</table></div>";
            div += tbl_body;
        })
    })
    .done(function() { $("#resolverResults").html(div); })
    .fail(function() { $("#resolverResults").html("<div>Unable to resolve " + $("#identifier").val() + "</div>"); });
    */

}