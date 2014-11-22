gyfp.filter('popoverFileList', function() {

    var maxFiles = 10;
    return function(input) {
        var ret = "";

        // Check that we're dealing with an array
        if (!input.hasOwnProperty("length")) {
            console.warn("Expected an array, got ", input);
            return ret;
        }

        // Handle empty arrays
        if (input.length == 0) {
            return "No files";
        }

        // Build the real list
        else {
            input.forEach(function(elem, i) {

                // Limit the number of files to show to prevent the
                // popover from getting too crazy
                if (i < maxFiles) {
                    ret += elem.fileName + ", ";
                }
            });

            // Remove the trailing ', ' or add a '...' if there are
            // more files remaining
            if (input.length < 10) {
                ret = ret.substr(0, ret.length - 2);
            } else {
                ret += "...";
            }

            return ret;
        }
    };
});