/*global gapi, angular*/
var gyfp = angular.module("gyfp", ['ngRoute', 'ui.bootstrap', 'angular-ladda']);

gyfp.config(function ($routeProvider, $locationProvider) {
    //configure the routing rules here
    $routeProvider.when('/about', {
        controller: 'AboutController',
        templateUrl: 'js/partials/about.html'
    }).when('/manage/:folderId', {
        controller: 'ManageFolderController',
        templateUrl: 'js/partials/manage.html'
    }).when('/request/:requestId', {
        controller: 'TransferRequestController',
        templateUrl: 'js/partials/transfer.html'
    }).otherwise({
        redirectTo: '/about'
    });
});


var init = function() {
    console.log("Doing manual bootstrap");
    var gyfp_api = 'https://gimmeyourfilesplease.appspot.com/_ah/api';
    gapi.client.load('gyfp', 'v1', function() {
        // Also load the google plus API so that we can get the user's profile information
        gapi.client.load('plus','v1', function() {
            angular.bootstrap(document, ["gyfp"]);
            document.getElementById("preload-cover").style.display = "none";
        });
    }, gyfp_api);
};