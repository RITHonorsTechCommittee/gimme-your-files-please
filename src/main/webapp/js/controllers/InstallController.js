gyfp.controller("InstallController", ["$scope", '$routeParams', function($scope, $routeParams) {
    if ($routeParams.state == "error") {
        $scope.error = true;
        $scope.installText = "Try Again";
    } else {
        $scope.firstTime = true;
        $scope.installText = "install";
    }
}]);