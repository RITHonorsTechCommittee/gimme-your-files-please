gyfp.controller("AuthenticationController", ["$scope", "AuthenticationService", function($scope, authService) {
    $scope.authenticated = authService.isAuthenticated();

    if (!$scope.isAuthenticated) {
        authService.checkAuth();
    }

    $scope.$watch(authService.isAuthenticated, function(isAuthenticated) {
        $scope.authenticated = isAuthenticated;
    });

    $scope.authenticate = authService.authenticate;

}]);