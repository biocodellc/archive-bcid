// Populate a table of data showing resourceTypes
function populateResourceTypes(a) {
    var url = "/bcid/api/bcidService/resourceTypes";
    var jqxhr = $.ajax(url, function() {})
        .done(function(data) {
           $("#" + a).html(data);
        })
        .fail(function() {
            $("#" + a).html("Unable to load resourceTypes!");
        });
}

// Initialize the form
function populateSelect(a) {
    // Populate the SELECT box with resourceTypes from the server
    var url = "/bcid/api/bcidService/select/" + a;
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

    var jqxhr = $.getJSON("/bcid/api/resolver/" + $("#identifier").val() , function(data) {
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