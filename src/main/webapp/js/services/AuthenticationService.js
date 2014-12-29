/**
 * Authentication helper service to keep authentication code in a common
 * location.  Allows other controllers to easily check authentication status
 * and initiate authentication requests
 */
gyfp.service('AuthenticationService', function() {
    /**
     * Whether we have successfully authenticated
     * @type {boolean}
     */
    var authenticated = false,

        /**
         * Checks whether we have successfully authenticated
         *
         * @returns {boolean}  True, if we have authenticated
         */
        isAuthenticated = function() {
            return authenticated;
        },

        /**
         * Handles the response to an authentication request, setting the authentication status as appropriate
         *
         * @param authResponse  The response to the authentication request
         */
        handleAuthenticationRequest = function(authResponse) {
            authenticated = authResponse && !authResponse.error;

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
        makeAuthFunction = function(immediate) {
            return function() {
                gapi.auth.authorize({
                    client_id: "975557209634-fuq8i9nc7466p1nqn8aqv168vv3nttd0.apps.googleusercontent.com",
                    scope: ["https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/drive.readonly.metadata"],
                    immediate: immediate
                }, handleAuthenticationRequest);
            };
        };

    return {
        isAuthenticated: isAuthenticated,
        checkAuth: makeAuthFunction(true),
        authenticate: makeAuthFunction(false)
    };
});
