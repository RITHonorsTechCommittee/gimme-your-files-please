/**
 * Controller for displaying and managing ploite transfer requests
 */
gyfp.controller("TransferRequestController", ["$scope", "$modal", "$routeParams", "AuthenticationService", function($scope, $modal, $routeParams, authService) {

    /**
     * The request object to display
     *
     * @type {{id: string, files: array, requester: object, target: object}}
     */
    $scope.request = {
        id: $routeParams.requestId
    };


    $scope.loaded = false;
    $scope.authenticated = false;

    // Watch for change in authentication state
    $scope.$watch(authService.isAuthenticated, function(isAuthenticated) {
        $scope.authenticated = isAuthenticated;
        if (isAuthenticated) {
            $scope.loadRequest();
        } else {
            // If we are not authenticated, attempt to authenticate using
            // existing credentials to prevent having to click the login button
            authService.checkAuth();
        }
    });

    /**
     * Runs the load request for the transfer request
     */
    $scope.loadRequest = function() {
        gapi.client.gyfp.user.request
            .get({request: $scope.request.id})
            .execute($scope.applyRequest);
    };

    /**
     * Processes a request response from loadRequest
     *
     * @param request  the request object returned from the api call.
     */
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

        // This is not executed in the normal context of an angular call, so
        // we need to manually digest
        $scope.$apply();
    }
}]);