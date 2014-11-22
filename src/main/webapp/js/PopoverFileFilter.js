gyfp.filter('popoverFileList', ['$sce', function($sce) {

    var maxFiles = 10;
    return function(input) {
        if (input.hasOwnProperty("length") && input.length == 0) {
            return "No files";
        } else {
            var ret = "";
            input.forEach(function(elem, i) {
                if (i < maxFiles) {
                    ret += + elem.fileName + ", ";
                }
            });

            if (input.length < 10) {
                ret = ret.substr(0, ret.length - 2);
            } else {
                ret += "...";
            }

            return $sce.trustAsHtml(ret);
        }
    };
}]);