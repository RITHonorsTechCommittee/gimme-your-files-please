/**
 * Controller for displaying and managing polite transfer requests
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
    $scope.$on("AuthenticationService.AuthenticationChanged", function(event, isAuthenticated) {
        $scope.authenticated = isAuthenticated;
        if (isAuthenticated) {
            $scope.loadRequest();
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
    };

    $scope.removeAll = function() {
        $scope.
    };

    $scope.remove = function(toRemove) {
        var files = [];

        if (!toRemove.length) {
            toRemove = [toRemove];
        }

        toRemove.forEach(function(file) {
            files.push(file.fileId);
        });

        gapi.client.gyfp.user.request.remove({
            request: $scope.request.id,
            ids: [file.fileId]
        }).execute(function() {
            $scope.request.files = $scope.request.files.filter(function(f) {
                return f.fileId !== file.fileId;
            });

            $scope.$apply();
        });
    };
}]);