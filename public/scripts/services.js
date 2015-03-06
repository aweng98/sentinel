/**
 * Created by marco on 2/16/15.
 */


angular.module('sentinelApp')
    .factory('SentinelService', ['$http', 'UserService', '$log',
        function($http, usrSvc, $log) {

            // TODO: implement signature API
            var hash = function(apiKey) {
                return "FIXME::signed-with-" + apiKey;
            };

            var headers = function() {
                if (! usrSvc.isLoggedIn) {
                    $log.error("User is not logged in cannot validate request");
                    return null;
                }
                var username = usrSvc.user.username;
                var apiKey = usrSvc.user.apiKey;
                return {
                    'x-date': new Date(),
                    // FIXME: we will need to pass the request URL / content for proper signing
                    'Authorization': "username=" + username + ";hash=" + hash(apiKey)
                };
            };

            var service = {
                users: [],
                userData: null,
                getUsers: function() {
                    var config = {
                        method: 'GET',
                        url: '/user',
                        headers: headers('fake-user')
                    };
                    return $http(config).success(function (response) {
                        service.users = response;
                    });
                },
                getUser: function(userId) {
                    var config = {
                        method: 'GET',
                        url: '/user/' + userId,
                        headers: headers('fake-user')
                    };
                    return $http(config).success(function(user) {
                        service.userData = user;
                        service.userData.username = user.credentials.username;
                        service.userData.apiKey = user.credentials.api_key;
                        delete service.userData.credentials;
                    });
                },
                createUser: function(user) {
                    var config = {
                        method: 'POST',
                        url: '/user',
                        headers: headers('fake-user'),
                        data: user
                    };
                    return $http(config).success(function(response) {
                        console.log('User ' + response.credentials.username + ' created [' + response.id + ']');
                        return response;
                    });
                }
            };
            return service;
        }])
    .factory('UserService', ['$http', '$log', function($http, $log) {
        var service = {
            user: null,
            isLoggedIn: false,
            login: function(user) {
                $log.info("Logging in " + user.username);
                return $http.post('/login', user).then(
                    function(response) {
                        service.user = response.data;
                        var creds = response.data.credentials;
                        $log.debug("User " + creds.username + " logged in with API Key: " + creds.api_key);
                        service.user.username = creds.username;
                        service.user.apiKey = creds.api_key;
                        delete service.user.credentials;
                        service.isLoggedIn = true;
                    }
                );
            },
            logout: function() {
                service.isLoggedIn = false;
                service.user = null;
            }
        };
        return service;
    }]);