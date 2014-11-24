gyfp.controller('RevokeProgressController', ['$scope', '$modalInstance', 'users', 'folder', 'role',
    function ($scope, $modalInstance, users, folder, role) {
        $scope.title = "Processing...";
        $scope.progress = {
            overall: {
                total: 0,
                current: 0
            },
            user: {
                total: folder.files[users[0].permission].files[role].length,
                current: 0,
                index: 0
            }
        };

        users.forEach(function (user) {
            $scope.progress.overall.total += folder.files[user.permission].files[role].length;
        });

        $scope.folder = folder;
        $scope.role = role;
        $scope.user = users[0];

        $scope.isAborted = false;
        $scope.isFinished = false;
        $scope.isErrored = false;

        $scope.revoke = function () {
            var permission =  $scope.user.permission;
            gapi.client.gyfp.folders.revoke[role]({
                folder: folder.id,
                userId: permission
            }).execute(function (resp) {
                if (resp.hasOwnProperty("code")) {
                    $scope.error(resp);
                } else {
                    console.log("Got revoke response");
                    console.log(resp);

                    if (resp.files.hasOwnProperty(permission)
                        && resp.files[permission].hasOwnProperty('files')
                        && resp.files[permission].files.hasOwnProperty(role)) {
                        $scope.progress.user.current = $scope.progress.user.total - resp.files[permission].files[role].length;
                        $scope.user.files[role] = resp.files[permission].files[role];
                        console.log($scope.progress);
                    } else {
                        $scope.progress.user.current = $scope.progress.user.total;
                        $scope.user.files[role] = [];
                        console.log($scope.progress);
                    }


                    if ($scope.progress.user.current != $scope.progress.user.total && !$scope.isAborted) {
                        console.log("Would do again.");
                        $scope.$apply();
                        $scope.revoke();
                    } else {

                        $scope.progress.user.index += 1;

                        if ($scope.progress.overall.current + $scope.progress.user.current == $scope.progress.overall.total) {
                            $scope.title = "Finished";
                            $scope.isFinished = true;
                        } else {
                            $scope.progress.overall.current += $scope.progress.user.current;
                            if ($scope.progress.user.index < users.length) {
                                $scope.user = users[$scope.progress.user.index];
                                $scope.progress.user.current = 0;
                                $scope.progress.user.total = folder.files[$scope.user.permission].files[role].length;
                                $scope.revoke();
                            }
                        }

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

        console.log(users);

        $scope.abort = function () {
            $scope.isAborted = true;
        };

        $scope.close = function () {
            $modalInstance.close(users);
        };

        $scope.revoke();
    }]);
