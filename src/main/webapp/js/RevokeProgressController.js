gyfp.controller('RevokeProgressController', ['$scope', '$modalInstance', 'user', 'folder', 'role',
    function ($scope, $modalInstance, user, folder, role) {

        console.log(user, folder, role);
        $scope.numFiles = folder.files[user.permission].files[role].length;
        $scope.progress = 0;
        $scope.continue = true;
        $scope.error = false;
        $scope.folder = folder;
        $scope.role = role;

        $scope.closeEnabled = false;

        console.log("'Revoking' user access");


        $scope.revoke = function() {
            gapi.client.gyfp.folders.revoke[role]({
                folder: folder.id,
                userId: user.permission
            }).execute(function (resp) {
                if (resp.hasOwnProperty("code")) {
                    console.error("Error revoking read access");
                    console.error(resp);
                    $scope.error = "Error revoking " + role + " access!";
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
                        if ($scope.continue) {
                            console.log("Would do again.");
                            $scope.$apply();
                            $scope.revoke(role, user);
                        }
                    } else {
                        $scope.closeEnabled = true;
                        $scope.$apply();
                    }



                }
            });
        };

        console.log(user);

        $scope.abort = function() {
            $scope.continue = false;
        };

        $scope.close = function() {
            $modalInstance.close(user);
        };

        $scope.revoke();
    }]);
