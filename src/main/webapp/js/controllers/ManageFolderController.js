/**
 * Manage Folder Controller
 *
 * Provides functionality to manage the permissions for users of a specific
 * folder.
 */
gyfp.controller("ManageFolderController", ['$scope', '$modal', '$routeParams', 'AuthenticationService', '$filter', 'ngTableParams', function ($scope, $modal, $routeParams, authService, $filter, ngTableParams) {

    // Listen for authentication state changes so we know when to load the folder
    $scope.$on("AuthenticationService.AuthenticationChanged", function(event, isAuthenticated) {
        $scope.authenticated = isAuthenticated;
        if (isAuthenticated) {
            $scope.$apply();
            if (!$scope.loading) {
                $scope.load();
            }
        } else {
            $scope.folder.files = [];
            $scope.users = [];
            $scope.$apply();
        }
    });

    $scope.selectedUsers = {checked: false, items: {}};

    // Toggle all rows when the summary checkbox is checked
    $scope.$watch('selectedUsers.checked', function(value) {
        angular.forEach($scope.users, function(user) {
            if (angular.isDefined(user.permission)) {
                $scope.selectedUsers.items[user.permission] = value;
                user.selected = value;
            }
        });
    });

    // Handle the indeterminate checkbox for selecting all people
    $scope.$watch('selectedUsers.items', function(values) {
        if (!$scope.users) {
            return;
        }

        var checked = 0, unchecked = 0,
            total = $scope.users.length;

        angular.forEach($scope.users, function(item) {
            item.selected = values[item.permission];
            checked   += item.selected == true ? 1 : 0;
            unchecked += item.selected != true ? 1 : 0;
        });

        if ((unchecked == 0) || (checked == 0)) {
            $scope.selectedUsers.checked = (checked == total) && checked > 0;
        }
        angular.element("#select-all-table-checkbox").prop("indeterminate", (checked != 0 && unchecked != 0));

    }, true);


    /**
     * Updates the contents of the folder list from an api response
     *
     * @param folder  The new folder state
     */
    $scope.applyFolder = function(folder) {

        if (folder.error) {
            $scope.isErrored = true;
            if (folder.code === 404) {
                $scope.errorMessage = "The requested folder does not exist";
            } else if (folder.code === 403) {
                $scope.errorMessage = "You do not have permission to manage this folder";
            } else if (folder.code === 400){
                $scope.errorMessage = "Invalid folder";
            } else {
                $scope.errorMessage = "An unknown error occurred";
            }
        } else {
            $scope.errorMessage = "";
            $scope.isErrored = false;
            $scope.folder = {
                id: folder.id,
                files: folder.files
            };

            console.log(folder);
            $scope.users = [];
            for (var user in folder.files) {
                if (folder.files.hasOwnProperty(user)) {
                    if (folder.files[user].hasOwnProperty('files')) {
                        if (!folder.files[user].files.hasOwnProperty("reader")) {
                            folder.files[user].files.reader = [];
                        }

                        if (!folder.files[user].files.hasOwnProperty("writer")) {
                            folder.files[user].files.writer = [];
                        }

                        if (!folder.files[user].files.hasOwnProperty("owner")) {
                            folder.files[user].files.owner = [];
                        }
                        $scope.users.push(folder.files[user]);
                    }
                }
            }
        }

        $scope.tableParams.reload();
        $scope.loading = false;
        $scope.$apply();
    };

    /**
     * Executes a polite transfer request for the given user's files.
     *
     * @param user  The target user object
     */
    $scope.ask = function(user) {
        if(user.hasActiveRequest) {
            console.log('Not asking '+user.name+' again');
            return;
        }
        console.log("'Asking' user for files");
        console.log(user);
        user.inProgress = true;
        user.hasActiveRequest = true;
        gapi.client.gyfp.folders.transfer.polite({folder: $scope.folder.id, users: [user.permission]})
            .execute(function(resp){
                user.inProgress = false;
                if(resp.error ||        resp.result.error) {
                    $scope.isErrored = true;
                    $scope.errorMessage += ($scope.errorMessage?"<br>":'') + "Failed to ask "+user.name;
                    user.hasActiveRequest = false;
                }
                console.log(resp);
                $scope.$apply();
            }
        );
    };

    /**
     * Executes a hostile takeover request for the given user's files.
     *
     * @param user  The target user object
     */
    $scope.force = function(user) {
        console.log("'Forcing' transfer");
        console.log(user);
    };

    /**
     * Revokes a users read/write permissions within a folder
     *
     * @param role  The role which will be revoked
     * @param user  The target user object
     */
    $scope.revoke = function(role, user) {
        var modalInstance = $modal.open({
            templateUrl: '../includes/RevokeDialog.html',
            controller: 'RevokeProgressController',
            size: 'sm',
            resolve: {
                users: function() {
                    return [user];
                },
                role: function() {
                    return role;
                },
                folder: function() {
                    return $scope.folder;
                }
            },
            keyboard: false,
            backdrop: 'static'
        });

        modalInstance.result.then(function(result) {
            console.log(result)
        }, function() {
            console.log('dismissed');
        });
    };

    /**
     * Executes a polite transfer request for all selected users
     */
    $scope.askAll = function() {
        console.log("Ask all.");
        console.log($scope.getSelectedUsers());
        $scope.getSelectedUsers().forEach(function(user){
            $scope.ask(user);
        })
    };

    /**
     * Executes a hostile takeover request for all selected users
     */
    $scope.forceAll = function() {
        console.log("Force all.");
        console.log($scope.getSelectedUsers());
    };

    /**
     * Revokes all selected users read/write permissions within a folder
     *
     * @param role  The role which will be revoked  (reader or writer)
     */
    $scope.revokeAll = function(role) {
        var modalInstance = $modal.open({
            templateUrl: '../includes/RevokeDialog.html',
            controller: 'RevokeProgressController',
            size: 'sm',
            resolve: {
                users: function () {
                    return $scope.getSelectedUsers();
                },
                role: function () {
                    return role;
                },
                folder: function () {
                    return $scope.folder;
                }
            },
            keyboard: false,
            backdrop: 'static'
        });
    };

    /**
     * Gets a list of the users that are currently selected
     * @returns {Array.<Users>}
     */
    $scope.getSelectedUsers = function() {
        return $scope.users.filter(function(user) {
            return user.selected;
        });
    };

    /**
     * Checks if any of the selected users own files
     *
     * @returns {boolean}  True if at least one selected user owns at least one file
     */
    $scope.isOwnerSelected = function() {
        return $scope.getSelectedUsers().some(function(user) {
            return user.files.owner.length > 0;
        });
    };

    /**
     * Checks if any of the selected users have read access to files
     *
     * @returns {boolean}  True if at least one selected user can read at least one file
     */
    $scope.isReaderSelected = function() {
        return $scope.getSelectedUsers().some(function (user) {
            return user.files.reader.length > 0;
        });
    };

    /**
     * Checks if any of the selected users have write access to files
     *
     * @returns {boolean}  True if at least one selected user ca n write at least one file
     */
    $scope.isWriterSelected = function() {
        return $scope.getSelectedUsers().some(function(user) {
            return user.files.writer.length > 0;
        });
    };

    var refreshFunction = function(force) {
        return function() {
            $scope.loading = true;
            console.log("Refreshing the folder");
            gapi.client.gyfp.folders.get({
                id: $scope.folder.id,
                ignoreCache: force
            }).execute(function(resp) {
                $scope.applyFolder(resp);
            });
        };
    };

    /**
     * Forces a refresh of the contents of the folder
     */
    $scope.refresh = refreshFunction(true);
    $scope.load = refreshFunction(false);

    $scope.loading = false;
    $scope.authenticated = authService.isAuthenticated();
    $scope.selectAll = false;
    $scope.users = [];

    $scope.folder = {
        id: $routeParams.folderId
    };

    // Setup ng-table (and receive sorting and such!!)
    $scope.tableParams = new ngTableParams({
        page: 1,            // show first page
        count: 10,          // count per page
        sorting: {
            "files.owner.length": 'desc'     // initial sorting
        }
    }, {
        total: function() { console.log($scope.users.length); return $scope.users.length; }, // length of data
        getData: function($defer, params) {
            params.total($scope.users.length);

            // use built-in angular filter
            var orderedData = params.sorting() ?
                $filter('orderBy')($scope.users, params.orderBy()) :
                $scope.users;

            $defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        }
    });



    if ($scope.authenticated) {
        $scope.load();
    }
}]);