
angular.module('sentinelApp', [])

    .controller('SentinelCtrl', ['$http', '$log', function (http, log) {
        var self = this;
        self.message = 'Sentinel Controller loaded';
        self.msgClass = 'normal';
        self.header = "Sentinel Users";
        self.heading = "h3";
        self.users = [];

        self.login = function () {
            console.log("Logging in " + self.user.username);
            self.errMsg = null;
            http.post('/login', self.user).then(
                function(response) {
                    console.log('User authenticated');
                    var user = response.data;
                    self.user.apiKey = user.credentials.api_key;
                    self.user.username = user.credentials.username;
                    console.log("User " + self.user.username + " logged in," +
                                "with API Key: " + self.user.apiKey);
                    self.getUsers();
                },
                function(err) {
                    console.log('There was an error: ' + err);
                    self.errMsg = err.data.error;
                }
            );
        };

        self.authenticated = function () {
            return self.user != null && self.user.apiKey != null;
        };

        self.logout = function() {
            self.user = null;
        };

        hash = function() {
            return "faferouaouvekarueiu";
        };

        headers = function() {
            return {
                'x-date': new Date(),
                'Authorization': "username=" + self.user.username + ";hash=" + hash()
            };
        };

        self.getUsers = function() {
            var config = {
                method: 'GET',
                url: '/user',
                headers: headers()
            };
            http(config).success(function (response) {
                self.users = response;
            }).error(function (errResponse) {
                console.error("Could not retrieve users: " + errResponse);
                self.errMsg = errResponse;
            });
        };

        self.getUser = function(userId) {
            var config = {
                method: 'GET',
                url: '/user/' + userId,
                headers: headers()
            };
            http(config).success(function (response) {
                self.userData = response;
            }).error(function (errResponse) {
                console.error("Could not retrieve user [" + userId + "]: " + errResponse);
                self.errMsg = errResponse;
            });
        };
        self.userDataClass = function(isActive) {
            return {
                activeUser: isActive,
                inactiveUser: !isActive
            };
        };
    }]);
