angular.module('sentinelApp')
    .controller('SentinelCtrl', ['UserService', 'SentinelService', '$log', '$location',
        function (UserService, SentinelService, $log, $location) {
            var self = this;
            $log.info('Sentinel Controller loaded');
            self.userService = UserService;
            self.user = UserService.user;

            self.msgClass = 'normal';
            self.header = "Sentinel Users";
            self.heading = "h3";
            self.users = SentinelService.users;
            self.userData = SentinelService.userData;

            self.login = function () {
                $log.info("Logging in " + self.user.username);
                self.errMsg = null;
                UserService.login(self.user).then(function () {
                    self.user = UserService.user;
                    $log.debug("Logged in: " + self.user.username);
                    self.errMsg = null;
                    $location.path('/listUsers');
                }, function (error) {
                    if (error.status === 401) {
                        $log.error("Cannot authenticate " + self.user.username);
                    } else {
                        $log.error("Error (" + error.data.error + ") encountered while logging in user "
                            + self.user.username);
                    }
                    self.errMsg = error.data.error;
                });
            };

            self.authenticated = function () {
                return UserService.isLoggedIn;
            };

            self.logout = function () {
                UserService.logout();
                self.user = UserService.user;
            };

            // API Functionality

            self.getUsers = function () {
                SentinelService.getUsers().error(function (errResponse) {
                    $log.error("Could not retrieve users: " + errResponse);
                    self.errMsg = errResponse;
                }).success(function() {
                    self.users = SentinelService.users;
                })
            };

            self.getUser = function (userId) {
                SentinelService.getUser(userId).success(function () {
                    self.userData = SentinelService.userData;
                    $log.info("Retrieved " + self.userData.username);
                    $location.path('/showUser/' + userId);
                }).error(function (errResponse) {
                    $log.error("Could not retrieve user [" + userId + "]: " + errResponse);
                    self.errMsg = errResponse;
                });
            };

            self.createUser = function(user) {
              $log.info('Creating a new user for: ' + user.credentials.username);
                SentinelService.createUser(user).error(function(errResponse) {
                    $log.error("Could not create a new user [" + user.credentials.username + "]: " + errResponse);
                    self.errMsg = errResponse;
                }).success(function() {
                    $log.info("User created: " + user.credentials.username);
                    self.userMsg = 'Success: User ' + user.credentials.username + ' created';
                    $location.path('/');
                })

            };

            self.userDataClass = function (isActive) {
                return {
                    activeUser: isActive,
                    inactiveUser: !isActive
                };
            };

            self.getSignedUserAcct = function() {
                self.getUser(self.user.id);
                $log.info('Current user: ', self.userData.first_name);
            };

            self.path = function(newPath) {
                $location.path(newPath);
            }
        }]);
