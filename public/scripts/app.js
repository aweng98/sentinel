
angular.module('sentinelApp', ['ngRoute'])

    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/', {
            template: '<p>TODO: create a default page</p>'
        }).when('/listUsers', {
            templateUrl: '/web/list-users.html',
            controller: 'SentinelCtrl as ctrl',
            resolve: {
                async: ['SentinelService', function(apiService) {
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
        });
        $routeProvider.otherwise({
            redirectTo: '/'
        })
    }]);
