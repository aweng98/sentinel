
angular.module('sentinelApp', ['ngRoute'])
    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/', {
            template: '<h3>Main page content goes here</h3>'
        }).when('/listUsers', {
            templateUrl: '/web/list-users.html',
            controller: 'SentinelCtrl as ctrl',
            resolve: {
                async: ['SentinelService', '$log', function(apiService, $log) {
                    $log.debug("async in list users - calling apiService")
                    return apiService.getUsers();
                }]
            }
        }).when('/showUser/:userId', {
            templateUrl: '/web/show-user.html',
            controller: 'SentinelCtrl as ctrl',
            resolve: {
                async: ['SentinelService', '$route', '$log', function(apiService, $route, $log) {
                    var userId = $route.current.params.userId;
                    $log.debug("Getting info about user ", userId)
                    return apiService.getUser(userId);
                }]
            }
        }).when('/createUser', {
            templateUrl: '/web/create-user.html',
            controller: 'SentinelCtrl as ctrl'
        }).when('/listOrgs', {
            templateUrl: '/web/list-orgs.html',
            controller: 'SentinelCtrl as ctrl',
            resolve: {
                async: ['SentinelService', '$log', function(apiService, $log) {
                    $log.debug("Listing all orgs for the user");
                    return apiService.getOrgs();
                }]
            }
        }).when('/createOrg', {
            templateUrl: '/web/create-org.html',
            controller: 'SentinelCtrl as ctrl'
        });
        $routeProvider.otherwise({
            redirectTo: '/listUsers'
        })
    }]);
