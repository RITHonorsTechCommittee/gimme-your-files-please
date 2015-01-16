/**
 * Authentication helper service to keep authentication code in a common
 * location.  Allows other controllers to easily check authentication status
 * and initiate authentication requests
 */
gyfp.service('AuthenticationService', ['$rootScope', function($rootScope) {
    /**
     * Whether we have successfully authenticated
     * @type {boolean}
     */
    var authenticated = false,
        user = {},

        /**
         * Checks whether we have successfully authenticated
         *
         * @returns {boolean}  True, if we have authenticated
         */
        isAuthenticated = function() {
            return authenticated;
        },

        getUser = function() {
            return user;
        },

        /**
         * Handles the response to an authentication request, setting the authentication status as appropriate
         *
         * @param authResponse  The response to the authentication request
         */
        handleAuthenticationRequest = function(authResponse) {
            authenticated = authResponse && authResponse.status && authResponse.status.signed_in;
            console.log("Authenticated: ", authenticated, authResponse);
            $rootScope.$broadcast("AuthenticationService.AuthenticationChanged", authenticated);

            if (authenticated) {
                gapi.client.plus.people.get({
                    'userId': 'me'
                }).execute(function (me) {
                    user = me;
                    $rootScope.$broadcast("AuthenticationService.UserLoaded", user);
                });
            } else {
                user = {};
                $rootScope.$broadcast("AuthenticationService.UserLoaded", user);
                console.log("Broadcast")
            }
        },

        /**
         * Helper function to create authentication functions.
         *
         * An immediate auth request does not show the authentication popups.
         * It uses any existing credentials and checks the authentication
         * status.  It can be used to quickly verify the authentication status
         * of a session.
         *
         * A non-immediate request will show a popup for authentication if
         * necessary.
         *
         * @param immediate  Whether this is an immediate auth request or not.
         * @returns {Function}  The authentication function (which takes no arguments)
         */
        makeAuthFunction = function(immediate)
        {
            return function() {
                console.log("Initiating authentication request");

                gapi.auth.authorize({
                    client_id: "975557209634-fuq8i9nc7466p1nqn8aqv168vv3nttd0.apps.googleusercontent.com",
                    scope: ["profile", "https://www.googleapis.com/auth/drive"],
                    immediate: immediate,
                    authuser:""
                }, handleAuthenticationRequest);
            };
        };

    return {
        isAuthenticated: isAuthenticated,
        checkAuth: makeAuthFunction(true),
        authenticate: makeAuthFunction(false),
        deauthenticate: gapi.auth.signOut,
        getUser: getUser
    };
}]);
