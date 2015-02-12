gyfp.controller("AuthenticationController", ["$scope", "AuthenticationService", function($scope, authService) {
    $scope.authenticated = authService.isAuthenticated();

    $scope.operationRunning = false;

    if (!$scope.authenticated) {
        authService.checkAuth();
    }

    // Subscribe to changes in authentication state
    $scope.$on("AuthenticationService.AuthenticationChanged", function(event, isAuthenticated) {
        console.log("Updated Auth Status", isAuthenticated);
        $scope.authenticated = isAuthenticated;
        $scope.displayName = "";
        $scope.email = "";
        $scope.operationRunning = false;
        $scope.$apply();
    });

    // Subscribe to changes in user information
    $scope.$on("AuthenticationService.UserLoaded", function(event, user) {
        console.log("Updated User", user);

        $scope.displayName = user.displayName || "";

        if (user.emails && user.emails.length > 0) {
            $scope.email = user.emails[0].value;
        } else {
            $scope.email = "";
        }
        $scope.$apply();
    });

    $scope.authenticate = function() {
        $scope.operationRunning = true;
        authService.authenticate();
    };

    $scope.deauthenticate = function() {
        $scope.operationRunning = true;
        authService.deauthenticate();

    };
}]);