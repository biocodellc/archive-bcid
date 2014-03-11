/** Process submit button for Data Group Creator **/
function dataGroupCreatorSubmit() {
    $( "#dataGroupCreatorResults" ).html( "Processing ..." );
    /* Send the data using post */
    var posting = $.post( "/id/groupService", $("#dataGroupForm").serialize() );
    results(posting,"#dataGroupCreatorResults");
}

/** Process submit button for Data Group Creator **/
function expeditionCreatorSubmit() {
    $( "#expeditionCreatorResults" ).html( "Processing ..." );
    /* Send the data using post */
    var posting = $.post( "/id/expeditionService", $("#expeditionForm").serialize() );
    results(posting,"#expeditionCreatorResults");
}

/** Process submit button for Creator **/
function creatorSubmit() {
    $( "#creatorResults" ).html( "Processing ..." );
    /* Send the data using post */
    var posting = $.post( "/id/elementService/creator", $("#localIDMinterForm").serialize() );
    results(posting, "#creatorResults");
}

/** Generic way to display results from creator functions, relies on standardized JSON **/
function results(posting, a) {
    // Put the results in a div
    posting.done(function( data ) {
        var content = "<table>";
        content += "<tr><th>Results</th></tr>"
        content += "<tr><td>"+data+"</td></tr>";
        //$.each(data, function(k,v) {
        //    content += "<tr><td>"+v+"</td></tr>";
        //})
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
        var url = "/id/groupService/metadata/" + $("#datasetList").val();
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

// Populate Div element from a REST service with HTML
function populateDivFromService(url,elementID,failMessage)  {
    if (!elementID.contains('#')) {
        elementID = '#' + elementID
    }
    var jqxhr = $.ajax(url, function() {})
        .done(function(data) {
           $(elementID).html(data);
        })
        .fail(function() {
            $(elementID).html(failMessage);
        });
}

// Populate a table of data showing resourceTypes
function populateResourceTypes(a) {
    var url = "/id/elementService/resourceTypes";
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
        var url = "/id/groupService/list/";
    // Project Service Call
    } else if (a == "adminProjects") {
        var url = "/id/projectService/admin/list/";
    } else if (a == "userList") {
        var url = "/id/userService/list/";
    // bcid Service Call
    } else {
        var url = "/id/elementService/select/" + a;
    }

    // get JSON from server and loop results
    var jqxhr = $.getJSON(url, function() {})
        .done(function(data) {
            var options = '';
            $.each(data[0], function(key, val) {
                options+='<option value="' + key + '">' +val + '</option>';
            });
            $("#" + a).html(options);
            if (a == "adminProjects") {
                $("." + a).html(options);
            }
        });
}

// Take the resolver results and populate a table
function resolverResults() {
    window.location.replace("/id/metadata/" + $("#identifier").val());
}

function updateProjectConfig(select) {
    var val = select.val()
    $('a', '#projectConfig').attr('href', '/bcid/secure/project.jsp?projectId=' + val);

    var jqxhr = $.getJSON('/id/projectService/config/' + val, function() {})
        .done(function(data) {
            $('#projectTitle').text(data[0]['title']);
            $('#projectAbstract').text(data[0]['abstract']);
            $('#projectValidationXML').text(data[0]['validation_xml']);
        });
}

function getQueryParam(sParam) {
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam) {
            return sParameterName[1];
        }
    }
}

function populateProjectPage(username) {
    var jqxhr = $.getJSON('/id/projectService/admin/list')
        .done(function(data) {
            var html = '<h2>' + username + 's Projects</h2>\n';
            var expandTemplate = '<br>\n<a class="expand-content" id="{project}-{section}" href="javascript:void(0);">\n'
                                + '\t <img src="../images/right-arrow.png" id="arrow" class="img-arrow">{text}'
                                + '</a>\n';
            $.each(data[0], function(key, val) {
                var project = val.replace(' ', '_', 'g')

                html += expandTemplate.replace('{text}', val).replace('-{section}', '');
                html += '<div id="{project}" class="toggle-content">';
                html += expandTemplate.replace('{text}', 'Configuration').replace('{section}', 'config').replace('<br>\n', '');
                html += '<div id="{project}-config" class="toggle-content">Loading Project Configuration...</div>';
                html +=  expandTemplate.replace('{text}', 'Users').replace('{section}', 'users');
                html += '<div id="{project}-users" class="toggle-content">Loading Users...</div>';
                html += '</div>\n';

                // add current project to element id
                html = html.replace('{project}', project, 'g');
            });
            if (!html.contains("expand-content")) {
                html += 'You are not an admin for any project.';
            }
            $(".sectioncontent").html(html);

            // attach toggle function to each project
            $(".expand-content").click(function() {
                projectToggle(this.id)
            });
        });
}

function projectToggle(id) {
    if ($('.toggle-content#'+id).is(':hidden')) {
        $('.img-arrow', '#'+id).attr("src","../images/down-arrow.png");
    } else {
        $('.img-arrow', '#'+id).attr("src","../images/right-arrow.png");
    }
    // check if we've loaded this section, if not, load from service
    if ((id.contains("config") || id.contains("users")) && $('div#' + id).children().length == 0) {
        populateConfigOrUsers(id);
    }
    $('.toggle-content#'+id).slideToggle('slow');
}

function populateConfigOrUsers(id) {
    // load config table from REST service
    var title = id.replace('_', ' ', 'g').split('-')[0];
    $.getJSON('/id/projectService/list')
        .done(function(data) {
            var projectID = null;
            $.each(data['projects'], function(key, project) {
                if (project['project_title'] == title) {
                     projectID = project['project_id'];
                     return false;
                }
            });

            if (id.contains("config")) {
                populateDivFromService(
                    '/id/projectService/configAsTable/' + projectID,
                    'div#' + id,
                    'Unable to load this project\'s configuration from server.');
            } else {
                populateDivFromService(
                    '/id/userService/listProjectUsersAsTable/' + projectID,
                    'div#' + id,
                    'Unable to load this project\'s users from server.')
            }
        });
}