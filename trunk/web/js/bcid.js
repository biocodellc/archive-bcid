// Initialize the form
function populateSelect(a) {
    // Populate the SELECT box with resourceTypes from the server
    var url = "/bcid/rest/bcidService/select/resourceTypes";
    var jqxhr = $.getJSON(url, function() {})
        .done(function(data) {
            var options = '';

            $.each(data[0], function(key, val) {
                options+='<option value="' + key + '">' +val + '</option>';
            });
            $("#" + a).html(options);
        });
}