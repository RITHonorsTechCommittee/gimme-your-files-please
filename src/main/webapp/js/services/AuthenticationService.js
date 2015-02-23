/**
 * Authentication helper service to keep authentication code in a common
 * location.  Allows other controllers to easily check authentication status
 * and initiate authentication requests
 */
gyfp.service('AuthenticationService', ['$rootScope', '$location', function($rootScope, $location) {
    /**
     * Whether we have successfully authenticated
     * @type {boolean}
     */
    var authenticated = false,
        hasCheckedAuth = false,
        signedOut = false,
        installedSuccessfully = false,
        user = {},

        /**
         * Checks whether we have successfully authenticated
         *
         * @returns {boolean}  True, if we have authenticated
         */
        isAuthenticated = function() {
            return installedSuccessfully;
        },

        /**
         * Checks whether we have successfully been installed into drive and had permissions cached.
         *
         * This is critical to the operation of the API calls.  We need to install into drive and get offline access
         * otherwise none of the serverside calls will work.
         *
         * @returns {boolean}  True if the app has been installed, false otherwise
         */
        isInstalled = function () {
            return isInstalled;
        },

        /**
         * Checks if the user has attempted to sign out from the application to prevent immediate login
         *
         * @returns {boolean}  True, if we have signed out
         */
        hasSignedOut = function() {
            return signedOut;
        },

        /**
         * Helper function to return the currently logged in user
         *
         * @returns {{}}
         */
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
                checkInstallation();
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
        },

        /**
         * Makes a request to the verify API endpoint.  If the API responds as successful then we have a cached
         * credential that is able to grant a token for us.  If not, we will not be able to preform any api calls
         * involving drive!
         */
        checkInstallation = function() {
            gapi.client.gyfp.user.verify.installation().execute(function(resp) {
                installedSuccessfully = resp.success || false;

                if (!installedSuccessfully) {
                    $rootScope.$apply(function() {
                        $location.path("/install/");
                    });
                }
                $rootScope.$broadcast("AuthenticationService.InstallationChanged", installedSuccessfully);
            });
        };

    return {
        isAuthenticated: isAuthenticated,
        isInstalled: isInstalled,
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
