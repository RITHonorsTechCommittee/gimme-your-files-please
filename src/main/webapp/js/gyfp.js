/*global gapi, angular*/
var gyfp = angular.module("gyfp", ['ngRoute', 'ui.bootstrap', 'angular-ladda']);

gyfp.config(function ($routeProvider, $locationProvider) {
    //configure the routing rules here
    $routeProvider.when('/about', {
        controller: 'AboutController',
        templateUrl: 'js/partials/about.html'
    }).when('/manage/:folderId', {
        controller: 'FolderListController',
        templateUrl: 'js/partials/folder-list.html'
    }).when('/request/:requestId', {
        controller: 'FileListController',
        templateUrl: 'js/partials/transfer.html'
    }).otherwise({
        redirectTo: '/about'
    });
});


gyfp.controller("FileListController", ["$scope", "$modal", "$routeParams", "AuthenticationService", function($scope, $modal, $routeParams, authService) {
    $scope.request = {
        id: $routeParams.requestId
    };

    $scope.loaded = false;

    $scope.$watch(authService.isAuthenticated, function(isAuthenticated) {
        if (isAuthenticated) {
            $scope.loadRequest();
        }
    });

    $scope.loadRequest = function() {
        gapi.client.gyfp.user.request.get({request: $scope.request.id}).execute($scope.applyRequest);
    };

    $scope.applyRequest = function(request) {
        $scope.loaded = true;
        if (request && request.error) {
            $scope.isErrored = true;
            $scope.errorMessage = request.message;
        } else {
            $scope.request.files = request.files;
            $scope.request.requester = request.requestingUser;
            $scope.request.target = request.targetUser;
        }

        $scope.$apply();
    }
}]);

var init = function() {
    console.log("Doing manual bootstrap");
    var gyfp_api = 'https://gimmeyourfilesplease.appspot.com/_ah/api';
    gapi.client.load('gyfp', 'v1', function() {
        angular.bootstrap(document, ["gyfp"]);
    }, gyfp_api);
};