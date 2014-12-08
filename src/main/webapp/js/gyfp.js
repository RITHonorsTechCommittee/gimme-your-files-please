/*global gapi, angular*/
var gyfp = angular.module("gyfp", ['ui.bootstrap', 'angular-ladda']);

gyfp.controller("FileListController", ['$scope', '$modal', function ($scope, $modal) {

    /**
     * Updates the contents of the folder list from an api response
     *
     * @param folder  The new folder state
     */
    $scope.applyFolder = function(folder) {
        $scope.folder = {};
        $scope.folder.id = folder.id;
        $scope.folder.files = folder.files;
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

        $scope.folderLoading = false;
        $scope.loaded_users = true;
        $scope.$apply();
    };

    /**
     * Executes a polite transfer request for the given user's files.
     *
     * @param user  The target user object
     */
    $scope.ask = function(user) {
        console.log("'Asking' user for files");
        console.log(user);
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

    $scope.isSelectAllIndeterminate = function() {
        var numSelectedUsers = $scope.getSelectedUsers().length;
        return numSelectedUsers > 0 && numSelectedUsers != $scope.users.length;
    };

    $scope.toggleSelectAll = function() {
        $scope.users.forEach(function(user) {
            user.selected = $scope.selectAll;
        })
    };

    /**
     * Forces a refresh of the contents of the folder
     */
    $scope.refresh = function() {
        $scope.folderLoading = true;
        console.log("Refreshing the folder");
        gapi.client.gyfp.folders.get({
            id: $scope.folder.id,
            ignoreCache: true
        }).execute(function(resp) {
            $scope.applyFolder(resp);
        });
    };

    $scope.selectAll = false;
    $scope.loaded_users = false;

    $scope.api_authenticated = function(resp) {
        $scope.api_ready = resp.status.signed_in;
        console.log("Got authentication response: ", $scope.api_ready);

        $scope.folderLoading = true;
        $scope.$apply();
        gapi.client.gyfp.folders.get({id: "0B0WTvx-f8-LZY0dxUGlwWmtSRHc"}).execute(function (resp) {
            $scope.applyFolder(resp);
        });
    };

    console.log("set_api_loaded");
    $scope.api_loaded = true;
    gapi.auth.authorize({client_id: "975557209634-fuq8i9nc7466p1nqn8aqv168vv3nttd0.apps.googleusercontent.com",scope:["https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/drive.readonly.metadata"], immediate:false}, $scope.api_authenticated);

    $scope.users = [];
}]);

var init = function() {
    console.log("Doing manual bootstrap");
    var gyfp_api = 'https://gimmeyourfilesplease.appspot.com/_ah/api';
    gapi.client.load('gyfp', 'v1', function() {
        angular.bootstrap(document, ["gyfp"]);

    }, gyfp_api);
};