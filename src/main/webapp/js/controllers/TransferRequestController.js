/**
 * Controller for displaying and managing polite transfer requests
 */
gyfp.controller("TransferRequestController", ["$scope", "$modal", "$routeParams", "$filter", "AuthenticationService", function($scope, $modal, $routeParams, $filter, authService) {

    /**
     * The request object to display
     *
     * @type {{id: string, files: array, requester: object, target: object}}
     */
    $scope.request = {
        id: $routeParams.requestId,
        files: []
    };


    $scope.loaded = false;
    $scope.authenticated = authService.isAuthenticated();
    $scope.removedFiles = [];
    $scope.transferring = false;
    $scope.transferComplete = false;
    $scope.deleting = false;

    // Watch for change in authentication state
    $scope.$on("AuthenticationService.AuthenticationChanged", function(event, isAuthenticated) {
        $scope.authenticated = isAuthenticated;
        if (isAuthenticated) {
            $scope.loadRequest();
        } else {
            $scope.request.files = [];
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

            // Clear out any data that may have been there before
            $scope.request = {
                id: $scope.request.id
            };
        } else {
            $scope.isErrored = false;
            $scope.errorMessage = "";
            $scope.request.files = request.files;
            $scope.request.requester = request.requestingUser;
            $scope.request.target = request.targetUser;
        }

        // This is not executed in the normal context of an angular call, so
        // we need to manually digest
        $scope.$apply();
    };

    $scope.delete = {
        isRunning: false,
        isDeleted: false,
        execute: function() {
            if (!$scope.delete.isRunning && !$scope.delete.isDeleted) {
                $scope.delete.isRunning = true;
                gapi.client.gyfp.user.request.delete({request: $scope.request.id}).execute(function(resp) {
                    console.log("Request deleted");
                    $scope.delete.isRunning = false;
                    $scope.delete.isDeleted = true;
                    $scope.$apply();
                });
            }
        }
    };

    /**
     * Checks if any files in the request are selected
     *
     * @returns {boolean}
     */
    $scope.hasSelectedFiles = function() {
        return $scope.request.files.some(function(file) {
            return file.selected;
        });
    };

    $scope.remove = function(file) {
        $scope._remove([file]);
    };

    $scope.removeFilesFromTransfer = function() {
        $scope._remove($scope._getSelectedFiles());
    };

    $scope.transferFiles = function() {
        $scope.transferring = true;
        gapi.client.gyfp.user.request.accept({request: $scope.request.id})
            .then(function() {
                $scope.$apply(function($scope) {
                    $scope.isErrored = false;
                    $scope.transferComplete = true;
                    $scope.transferring = false;
                });
            }, function(resp) {
                $scope.$apply(function ($scope) {
                    $scope.isErrored = true;
                    //TODO: make sure the error message is meaningful
                    $scope.errorMessage = resp.result.error.message;
                    $scope.transferring = false;
                });
            });
    };

    $scope.deleteRequest = function() {
        //TODO: confirm?
        $scope.deleting = true;
        gapi.client.gyfp.user.request.delete({request: $scope.request.id})
            .then(function() {
                $scope.$apply(function($scope) {
                    $scope.isErrored = false;
                    $scope.deleting = false;
                    $scope.request = false;
                });
            }, function(resp) {
                $scope.$apply(function ($scope) {
                    $scope.isErrored = true;
                    //TODO: make sure the error message is meaningful
                    $scope.errorMessage = resp.result.error.message;
                    $scope.deleting = false;
                });
            });
    };

    $scope._remove = function(fileArray) {
        console.log('Removing files');
        console.log(fileArray);
        fileArray.forEach(function(d) {
            d.loading = true;
        });
        var fileIds = fileArray.reduce(function(prev,next) {
            prev.push(next.fileId);
            return prev;
        },[]);
        console.log(fileIds);
        gapi.client.gyfp.user.request.remove({request: $scope.request.id, ids: fileIds})
            .then(function (resp) {
                console.log(resp);
                $scope.$apply(function ($scope) {
                    $scope.isErrored = false;
                    fileArray.forEach(function(d) {
                        d.loading = false;
                        $scope.request.files.splice($scope.request.files.indexOf(d), 1);
                        $scope.removedFiles.push(d);
                    });
                });
            }, function(resp) {
                console.error(resp);
                $scope.$apply(function ($scope) {
                    fileArray.forEach(function(d) {
                        d.loading = false;
                    });
                    $scope.isErrored = true;
                    //TODO: make sure the error message is meaningful
                    $scope.errorMessage = resp.result.error.message;
                });
            });
    };

    $scope._getSelectedFiles = function() {
        return $filter('filter')($scope.request.files,function(d){ return d.selected; });
    };

    if ($scope.authenticated) {
        $scope.loadRequest();
    }

}]);
