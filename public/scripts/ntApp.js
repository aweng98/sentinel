if (window.console) {
    console.log("Starting AngularJS Sentinel UI");
}


angular.module('sentinelApp', [])

    .controller('SentinelCtrl', [function () {
        var self = this;
        self.message = 'Sentinel Controller loaded';
        self.msgClass = 'normal';
        self.apiKey = null;

        self.username = '';
        self.password = '';

        self.login = function () {
            // TODO: use $get to execute an AJAX call to auth user
            if (self.password === 'pass') {
                self.apiKey = '1234';
            }
        };

        self.authenticated = function () {
            return self.apiKey != null;
        };

        self.logout = function() {
            self.apiKey = null;
        }
    }]);
