/*Global gapi */
gyfp.controller('RevokeProgressController', ['$scope', '$modalInstance', 'users', 'folder', 'role',
    function ($scope, $modalInstance, users, folder, role) {
        $scope.title = "Processing...";

        $scope.folder = folder;
        $scope.role = role;
        $scope.users = users;

        $scope.operationRunning = false;
        $scope.isAborted = false;
        $scope.isErrored = false;

        $scope.progress = {
            overall: {
                total: 0,
                current: 0
            },
            user: {
                total: 0,
                current: 0,
                index: 0
            }
        };

        /**
         * Updates the progress of the individual user permission modifications
         *
         * @param resp  The response to the API call
         */
        $scope.updateUserProgress = function (resp) {
            var permission = $scope.user.permission;

            if (resp.files.hasOwnProperty(permission)
                && resp.files[permission].hasOwnProperty('files')
                && resp.files[permission].files.hasOwnProperty(role)) {

                $scope.progress.user.current = $scope.progress.user.total - resp.files[permission].files[role].length;
                $scope.user.files[role] = resp.files[permission].files[role];
            } else {
                $scope.progress.user.current = $scope.progress.user.total;
                $scope.user.files[role] = [];
            }
        };

        /**
         * Checks if the current user has had all permissions revoked
         *
         * @returns {boolean}  True, if all the user's files have been processed
         */
        $scope.isUserDone = function () {
            return $scope.progress.user.current == $scope.progress.user.total;
        };

        /**
         * Checks if all requested users have had their permissions revoked
         *
         * @returns {boolean}  True, if all users are done processing
         */
        $scope.isFinished = function () {
            return $scope.progress.overall.current + $scope.progress.user.current == $scope.progress.overall.total
                || ($scope.isAborted && !$scope.operationRunning);
        };

        /**
         * Checks if the list of users this transfer is going to effect should be shown.
         *
         * @returns {boolean}  True, if the processing is done and there is more than one user.
         */
        $scope.showUserList = function () {
            return $scope.isFinished() && $scope.users.length > 1;
        };

        /**
         * Checks if there are more users to process
         *
         * @returns {boolean}  True, if there are more users to process
         */
        $scope.hasMoreUsers = function () {
            return $scope.progress.user.index < users.length;
        };

        $scope.revoke = function () {
            $scope.operationRunning = true;
            if (!$scope.isAborted) {
                gapi.client.gyfp.folders.revoke[role]({
                    folder: folder.id,
                    userId: $scope.user.permission
                }).execute(function (resp) {
                    $scope.operationRunning = false;
                    if (resp.hasOwnProperty("code")) {
                        $scope.error(resp);
                    } else {
                        console.log("Got revoke response");
                        console.log(resp);

                        $scope.updateUserProgress(resp);


                        if (!$scope.isUserDone()) {
                            $scope.revoke();
                        } else {

                            $scope.progress.user.index += 1;

                            if ($scope.isFinished()) {
                                $scope.title = "Finished";
                            } else {
                                $scope.progress.overall.current += $scope.progress.user.current;
                                if ($scope.hasMoreUsers()) {
                                    $scope.user = users[$scope.progress.user.index];
                                    $scope.progress.user.current = 0;
                                    $scope.progress.user.total = folder.files[$scope.user.permission].files[role].length;
                                    $scope.revoke();
                                }
                            }
                        }
                    }

                    $scope.$apply();
                });
            } else {
                $scope.operationRunning = false;
                $scope.$apply();
            }
        };

        $scope.error = function (resp) {
            console.error("Error revoking permissions");
            console.error(resp);
            if (resp.code >= 500) {
                $scope.title = "Internal Server Error";
                $scope.body = "The server encountered an error when trying to fulfill your request.  Please try again later.";
            } else if (resp.code >= 400) {
                $scope.title = "Bad Request";

                if (resp.code === 404) {
                    $scope.body = "The user " + $scope.user.email + " does not own any files in this folder";
                } else {
                    $scope.body = resp.message;
                }
            } else {
                $scope.title = "An unknown error occurred.";
                $scope.body = "Unable to complete the permission transfer.  Please try again later.";
            }
            $scope.isErrored = true;


            $scope.$apply();
        };

        console.log(users);

        $scope.abort = function () {
            $scope.isAborted = true;
        };

        $scope.close = function () {
            $modalInstance.close(users);
        };

        // Remove any checked users who do not have any files to begin with
        $scope.users = users.filter(function (user) {
            var files = folder.files[user.permission].files[role].length;
            $scope.progress.overall.total += files;

            return files > 0;
        });

        if ($scope.users.length == 0) {
            $scope.error({
                code: 400,
                message: "No users specified"
            });
        } else {
            $scope.user = $scope.users[0];
            $scope.progress.user.total = folder.files[$scope.user.permission].files[role].length;
            $scope.revoke();
        }
    }]);

