gyfp.controller("TransferRequestController", ["$scope", "$modal", "$routeParams", "AuthenticationService", function($scope, $modal, $routeParams, authService) {
    $scope.request = {
        id: $routeParams.requestId
    };

    $scope.loaded = false;
    $scope.authenticated = false;

    $scope.$watch(authService.isAuthenticated, function(isAuthenticated) {
        $scope.authenticated = isAuthenticated;
        if (isAuthenticated) {
            $scope.loadRequest();
        }
    });

    $scope.loadRequest = function() {
        gapi.client.gyfp.user.request.get({request: $scope.request.id}).execute($scope.applyRequest);
    };

    $scope.applyRequest = function(request) {
        $scope.loaded = true;
        if (request && request.error) {
            $scope.isErrored = true;
            $scope.errorMessage = request.message;
        } else {
            $scope.request.files = request.files;
            $scope.request.requester = request.requestingUser;
            $scope.request.target = request.targetUser;
        }

        $scope.$apply();
    }
}]);