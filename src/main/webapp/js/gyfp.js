/*global gapi, angular*/
var gyfp = angular.module("gyfp", []);

gyfp.controller("FileListController", ['$scope', function ($scope) {

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
     * @param user  The target user object
     */
    $scope.revoke = function(user) {
        console.log("'Revoking' user access");
        console.log(user);
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
     * Revokes read/write permissions for all selected users
     */
    $scope.revokeAll = function() {
        console.log("Revoke all.");
        console.log($scope.getSelectedUsers());
    };

    $scope.getSelectedUsers = function() {
        return $scope.users.filter(function(user) {
            return user.selected;
        });
    };

    $scope.isOwnerSelected = function() {
        return $scope.users.some(function(user) {
            return user.files.owner.length > 0;
        });
    };

    $scope.isReadWriteSelected = function() {
        return $scope.users.some(function(user) {
            return user.files.reader.length + user.files.writer.length > 0;
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

    $scope.selectAll = false;
    $scope.loaded_users = false;
    $scope.api_authenticated = function(resp) {
        $scope.api_ready = resp.status.signed_in;
        console.log("Got authentication response: ", $scope.api_ready);

        gapi.client.gyfp.folders.get({id: "0B0WTvx-f8-LZY0dxUGlwWmtSRHc"}).execute(function (resp) {
            console.log(resp);
            $scope.users = [];
            for (var user in resp.files) {
                if (resp.files.hasOwnProperty(user)) {
                    if (!resp.files[user].files.hasOwnProperty("reader")) {
                        resp.files[user].files.reader = [];
                    }

                    if (!resp.files[user].files.hasOwnProperty("writer")) {
                        resp.files[user].files.writer = [];
                    }

                    if (!resp.files[user].files.hasOwnProperty("owner")) {
                        resp.files[user].files.owner = [];
                    }
                    $scope.users.push(resp.files[user]);
                }
            }

            $scope.loaded_users = true;
            $scope.$apply();
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