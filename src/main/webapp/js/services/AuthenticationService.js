gyfp.service('AuthenticationService', function() {
    var authenticated = false,
        isAuthenticated = function() {
            return authenticated;
        },
        handleAuthenticationRequest = function(authResponse) {
            authenticated = authResponse && !authResponse.error;

        },
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
