gyfp.controller('RevokeProgressController', ['$scope', '$modalInstance', 'user', 'folder', 'role',
    function ($scope, $modalInstance, user, folder, role) {
        $scope.title = "Processing...";
        $scope.numFiles = folder.files[user.permission].files[role].length;
        $scope.progress = 0;
        $scope.isAborted = true;
        $scope.folder = folder;
        $scope.role = role;
        $scope.user = user;

        $scope.isFinished = false;
        $scope.isErrored = false;

        console.log("'Revoking' user access");


        $scope.revoke = function() {
            gapi.client.gyfp.folders.revoke[role]({
                folder: folder.id,
                userId: user.permission
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


                    if ($scope.progress != $scope.numFiles) {
                        if (!$scope.isAborted) {
                            console.log("Would do again.");
                            $scope.$apply();
                            $scope.revoke(role, user);
                        }
                    } else {
                        $scope.isFinished = true;
                        $scope.$apply();
                    }



                }
            });
        };

        $scope.error = function(resp) {
            console.error("Error revoking permissions");
            console.error(resp);
            if (resp.code >= 500) {
                $scope.title = "Internal Server Error";
                $scope.body = "The server encountered an error when trying to fulfill your request.  Please try again later.";
            } else if (resp.code >= 400) {
                $scope.title = "Bad Request";

                if (resp.code === 404) {
                    $scope.body = "The user " + $scope.user.email  + " does not own any files in this folder";
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

        $scope.abort = function() {
            $scope.isAborted = true;
        };

        $scope.close = function() {
            $modalInstance.close(user);
        };

        $scope.revoke();
    }]);
