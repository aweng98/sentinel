if (window.console) {
    console.log("Starting AngularJS Sentinel UI");
}


angular.module('sentinelApp', [])

    .controller('SentinelCtrl', [function () {
        var self = this;
        self.message = 'Sentinel Controller loaded';
        self.msgClass = 'normal';
        self.apiKey = null;

        self.login = function () {
            console.log("Logging " + self.user.username);
            // TODO: use $get to execute an AJAX call to auth user
            if (self.user.password === 'pass') {
                self.apiKey = '1234';
                console.log("User " + self.user.username + " logged in," +
                            "with API Key: " + self.apiKey);
            }
        };

        self.authenticated = function () {
            return self.apiKey != null;
        };

        self.logout = function() {
            self.user = null;
            self.apiKey = null;
        }
    }]);
