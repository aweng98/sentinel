if (window.console) {
    console.log("Starting AngularJS Sentinel UI");
}


angular.module('sentinelApp', [])

    .controller('SentinelCtrl', ['$http', '$log', function (http, log) {
        var self = this;
        self.message = 'Sentinel Controller loaded';
        self.msgClass = 'normal';

        self.login = function () {
            console.log("Logging in " + self.user.username);
            // TODO: use $get to execute an AJAX call to auth user
            http.post('/login', self.user).then(
                function(response) {
                    console.log('User authenticated');
                    var user = response.data;
                    self.user.apiKey = user.credentials.api_key;
                    console.log("User " + user.credentials.username + " logged in," +
                    "with API Key: " + self.user.apiKey);
                },
                function(err) {
                    console.log('There was an error: ' + err);
                }
            );
        };

        self.authenticated = function () {
            return self.user != null && self.user.apiKey != null;
        };

        self.logout = function() {
            self.user = null;
        }
    }]);
