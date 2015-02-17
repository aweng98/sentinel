
angular.module('sentinelApp', ['ngRoute'])

    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/', {
            template: '<p>TODO: create a default page</p>'
        }).when('/listUsers', {
            templateUrl: '/ui/list-users.html',
            controller: 'SentinelCtrl as ctrl'
        }).when('/showUser', {
            templateUrl: '/ui/show-user.html',
            controller: 'SentinelCtrl as ctrl'
        });
        $routeProvider.otherwise({
            redirectTo: '/'
        })
    }]);
