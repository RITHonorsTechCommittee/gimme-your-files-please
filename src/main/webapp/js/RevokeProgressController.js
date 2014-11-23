gyfp.controller('RevokeProgressController', ['$scope', '$modalInstance', 'users', 'folder', 'role',
    function ($scope, $modalInstance, users, folder, role) {
        var currentPermission;

        $scope.title = "Processing...";
        $scope.progress = {
            overall: {
                total: 0,
                current: 0
            },
            user: {
                total: folder.files[users[0].permission].files[role].length,
                current: 0,
                user: users[0],
                userIndex: 0
            }
        };

        users.forEach(function (user) {
            $scope.progress.overall += folder.files[user.permission].files[role].length;
        });

        $scope.folder = folder;
        $scope.role = role;
        $scope.user = users[0];

        $scope.isAborted = false;
        $scope.isFinished = false;
        $scope.isErrored = false;

        $scope.revoke = function () {
            gapi.client.gyfp.folders.revoke[role]({
                folder: folder.id,
                userId: $scope.progress.user.permission
            }).execute(function (resp) {
                if (resp.hasOwnProperty("code")) {
                    $scope.error(resp);
                } else {
                    console.log("Got revoke response");
                    console.log(resp);

                    if (resp.files.hasOwnProperty(user.permission)
                        && resp.files[user.permission].hasOwnProperty('files')
                        && resp.files[user.permission].files.hasOwnProperty(role)) {
                        $scope.progress = $scope.numFiles - resp.files[user.permission].files[role].length;
                        user.files[role] = resp.files[user.permission].files[role];
                        console.log($scope.progress);
                    } else {
                        $scope.progress = $scope.numFiles;
                        console.log($scope.progress);
                        user.files[role] = [];
                    }


                    if ($scope.progress != $scope.numFiles && !$scope.isAborted) {
                        console.log("Would do again.");
                        $scope.$apply();
                        $scope.revoke(role, user);
                    } else {
                        $scope.title = "Finished";
                        $scope.isFinished = true;
                        $scope.$apply();
                    }
                }
            });
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
            $scope.isFinished = true;
            $scope.isErrored = true;


            $scope.$apply();
        };

        console.log(user);

        $scope.abort = function () {
            $scope.isAborted = true;
        };

        $scope.close = function () {
            $modalInstance.close(user);
        };

        $scope.revoke();
    }]);
,
gyfp.controller('RevokeProgressController', ['$scope', '$modalInstance', 'users', 'folder', 'role'
