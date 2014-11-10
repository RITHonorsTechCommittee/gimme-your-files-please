var gyfp = angular.module("gyfp", []);

gyfp.controller("FileListController", ['$scope', '$window', function ($scope, $window) {

    console.log("init");

    $scope.requestedScopes = 1;
    $scope.loaded_users = false;
    $scope.api_authenticated = function(resp) {
        console.log(resp);
        $scope.requestedScopes -= 1;
        $scope.api_ready = $scope.requestedScopes === 0;

        if ($scope.api_ready) {
            console.log("Making real request.");
            gapi.client.gyfp.folders.get({id: "0B0WTvx-f8-LZY0dxUGlwWmtSRHc"}).execute(function (resp) {
                console.log(resp);
                $scope.raw_users = resp.files;
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
        } else {
            console.log(requestedScopes + " scopes remaining");
        }
    };

    console.log("set_api_loaded");
    $scope.api_loaded = true;
    gapi.auth.authorize({client_id: "975557209634-fuq8i9nc7466p1nqn8aqv168vv3nttd0.apps.googleusercontent.com",scope:["https://www.googleapis.com/auth/userinfo.email"], immediate:false}, $scope.api_authenticated);
    gapi.auth.authorize({client_id: "975557209634-fuq8i9nc7466p1nqn8aqv168vv3nttd0.apps.googleusercontent.com",scope:["https://www.googleapis.com/auth/drive.readonly.metadata"], immediate:false}, $scope.api_authenticated);

    $scope.users = [
        {
            selected: false,
            name: "Greg One",
            email: "greg@greg.greg",
            files: {
                owner: ["a", "b", "c"],
                reader: ["d", "e", "f"],
                writer: ["g", "h"]
            }
        },
        {
            selected: true,
            name: "Greg Two",
            email: "greg2@greg.greg",
            files: {
                owner: [],
                reader: ["f"],
                writer: ["g", "h"]
            }
        },
        {
            name: "Greg Three",
            email: "greg@greg.greg",
            files: {
                owner: ["d", "e", "f", "g", "h"],
                reader: [],
                writer: []
            }
        }
    ];
}]);

var init = function() {
    console.log("Doing manual bootstrap");
    var gyfp_api = 'https://gimmeyourfilesplease.appspot.com/_ah/api';
    gapi.client.load('gyfp', 'v1', function() {
        angular.bootstrap(document, ["gyfp"]);

    }, gyfp_api);
};