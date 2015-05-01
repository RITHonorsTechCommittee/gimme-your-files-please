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
            $scope.request.files = $filter('orderBy')(request.files,'fileName');
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
            gapi.client.gyfp.user.request.delete({request: $scope.request.id})
                .then(function() {
                    $scope.$apply(function($scope) {
                        $scope.isErrored = false;
                        $scope.delete.isRunning = false;
                        $scope.delete.isDeleted = true;
                        $scope.request = false;
                    });
                }, function(resp) {
                    $scope.$apply(function ($scope) {
                        $scope.isErrored = true;
                        //TODO: make sure the error message is meaningful
                        $scope.errorMessage = resp.result.error.message;
                        $scope.delete.isRunning = false;
                        $scope.delete.isDeleted = false;
                        console.log("Request deleted");
                        $scope.apply();
                    });
                });
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
        return $filter('filter')($scope.request.files,'selected');
    };

    $scope.selectedFiles = {checked: false, items: {}};

    // Toggle all rows when the summary checkbox is checked
    $scope.$watch('selectedFiles.checked', function(value) {
        angular.forEach($scope.request.files, function(file) {
            if (angular.isDefined(file.id)) {
                $scope.selectedFiles.items[file.id] = value;
                file.selected = value;
            }
        });
    });

    // Handle the indeterminate checkbox for selecting all people
    $scope.$watch('selectedFiles.items', function(values) {
        if (!$scope.request.files) {
            return;
        }

        var checked = 0, unchecked = 0,
            total = $scope.request.files.length;

        angular.forEach($scope.request.files, function(item) {
            item.selected = values[item.id];
            checked   += item.selected == true ? 1 : 0;
            unchecked += item.selected != true ? 1 : 0;
        });

        if ((unchecked == 0) || (checked == 0)) {
            $scope.selectedFiles.checked = (checked == total) && checked > 0;
        }
        angular.element("#select-all-table-checkbox").prop("indeterminate", (checked != 0 && unchecked != 0));

    }, true);

    // Setup ng-table (and receive sorting and such!!)
    $scope.tableParams = new ngTableParams({} , {
        total: function($defer, params) { $defer.resolve($scope.request.files); },
        getData: function($defer, params) {
            $defer.resolve($scope.request.files);
        }
    });

    if ($scope.authenticated) {
        $scope.loadRequest();
    }

}]);
