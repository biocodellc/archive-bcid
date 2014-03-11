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
    if (elementID.indexOf('#') == -1) {
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
                var project = val.replace(new RegExp(' ', 'g'), '_');

                html += expandTemplate.replace('{text}', val).replace('-{section}', '');
                html += '<div id="{project}" class="toggle-content">';
                html += expandTemplate.replace('{text}', 'Configuration').replace('{section}', 'config').replace('<br>\n', '');
                html += '<div id="{project}-config" class="toggle-content">Loading Project Configuration...</div>';
                html +=  expandTemplate.replace('{text}', 'Users').replace('{section}', 'users');
                html += '<div id="{project}-users" class="toggle-content">Loading Users...</div>';
                html += '</div>\n';

                // add current project to element id
                html = html.replace(new RegExp('{project}', 'g'), project);
            });
            if (html.indexOf("expand-content") == -1) {
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
    var divId = 'div#' + id
    if ((id.indexOf("config") != -1 || id.indexOf("users") != -1) && ($(divId).children().length == 0 || $('#submitForm', divId).length !== 0)) {
        populateConfigOrUsers(divId);
    }
    $('.toggle-content#'+id).slideToggle('slow');
}

function populateConfigOrUsers(id) {
    // load config table from REST service
    var title = id.replace('div#', '').replace(new RegExp('_', 'g'), ' ').split('-')[0];
    $.getJSON('/id/projectService/list')
        .done(function(data) {
            var projectID = null;
            $.each(data['projects'], function(key, project) {
                if (project['project_title'] == title) {
                     projectID = project['project_id'];
                     return false;
                }
            });

            if (id.indexOf("config") != -1) {
                populateDivFromService(
                    '/id/projectService/configAsTable/' + projectID,
                    id,
                    'Unable to load this project\'s configuration from server.');
                    $( document ).one("ajaxStop", function() {
                        $("#edit_config", id).click(function() {
                            populateDivFromService(
                                '/id/projectService/configEditorAsTable/' + projectID,
                                id,
                                'Unable to load this project\'s configuration editor.');
                            });
                            $( document ).one("ajaxStop", function() {
                                $('#configSubmit', id).click(function() {
                                    projectConfigSubmit(projectID, id);
                                 });
                            });
                    });
            } else {
                populateDivFromService(
                    '/id/projectService/listProjectUsersAsTable/' + projectID,
                    id,
                    'Unable to load this project\'s users from server.')
            }
        });
}

function projectUserSubmit(project_title) {
    var divId = 'div#' + project_title.replace(' ', '_');
    if ($('select option:selected', divId).val() == 0) {
        var project_id = $("input[name='project_id']", divId).val()
        populateDivFromService(
            '/id/userService/createFormAsTable',
            divId + '-users',
            'error fetching create user form');
        $( document ).one("ajaxStop", function() {
            $("input[name=project_id]", divId).val(project_id);
            $("#createFormButton", divId).click(function() {
                createUserSubmit(project_id, divId);
            });
        });
    } else {
        var jqxhr = $.post("/id/projectService/addUser", $('form', divId).serialize())
            .done(function(data) {
                populateConfigOrUsers(divId + '-users');
                if (data[0].error != null) {
                    $( document ).one("ajaxStop", function() {
                        $(".error", divId + '-users').html(data[0].error);
                    });
                }
            });
    }
}

function createUserSubmit(project_id, divId) {
    var jqxhr = $.post("/id/userService/create", $('form', divId).serialize())
        .done(function(data) {
            populateConfigOrUsers(divId + '-users');
            if (data[0].error != null) {
                $( document ).one("ajaxStop", function() {
                    $(".error", divId + '-users').html(data[0].error);
                });
            }
        });
}

function projectRemoveUser(userId, projectId, projectTitle) {
    var divId = 'div#' + projectTitle.replace(' ', '_');
    var jqxhr = $.getJSON("/id/projectService/removeUser/" + projectId + "/" + userId)
        .done (function(data) {
            populateConfigOrUsers(divId + '-users');
            if (data[0].error != null) {
                $( document ).one("ajaxStop", function() {
                    $(".error", divId + '-users').html(data[0].error);
                });
            }
        });
}

function projectConfigSubmit(project_id, divId) {
    var jqxhr = $.post("/id/projectService/updateConfig/" + project_id, $('form', divId).serialize())
        .done(function(data) {
            if (data[0].error != null) {
                $(".error", divId).html(data[0].error);
            } else {
                populateConfigOrUsers(divId);
            }
        });
}
