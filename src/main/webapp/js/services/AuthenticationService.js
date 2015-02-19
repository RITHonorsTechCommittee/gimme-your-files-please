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
        hasCheckedAuth = false,
        signedOut = false,
        user = {},

        /**
         * Checks whether we have successfully authenticated
         *
         * @returns {boolean}  True, if we have authenticated
         */
        isAuthenticated = function() {
            return authenticated;
        },

        /**
         * Checks if the user has attempted to sign out from the application to prevent immediate login
         *
         * @returns {boolean}  True, if we have signed out
         */
        hasSignedOut = function() {
            return signedOut;
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
                signedOut = false;
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
                if (immediate && hasCheckedAuth) {
                    return;
                }
                hasCheckedAuth = true;
                console.log("Initiating authentication request");

                gapi.auth.authorize({
                    client_id: "975557209634-fuq8i9nc7466p1nqn8aqv168vv3nttd0.apps.googleusercontent.com",
                    scope: ["profile", "email", "https://www.googleapis.com/auth/drive"],
                    immediate: immediate,
                    authuser: "",
                    accesstype: "offline",
                    cookiepolicy: "single_host_origin"
                }, handleAuthenticationRequest);
            };
        };

    return {
        isAuthenticated: isAuthenticated,
        hasSignedOut: hasSignedOut,
        checkAuth: makeAuthFunction(true),
        authenticate: makeAuthFunction(false),
        deauthenticate: function() {
            signedOut = true;
            gapi.auth.signOut();
            gapi.auth.setToken(null);
            $rootScope.$broadcast("AuthenticationService.AuthenticationChanged", false);
            authenticated = false;
        },
        getUser: getUser
    };
}]);
