angular.module('testApp', ['ngRoute'])

    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/', {
            template: '<h1>Home Page</h1>'
        }).when('/first', {
            template: '<p>First</p>',
            controller: 'MainCtrl as ctrl'
        }).when('/second', {
            template: '<p>second</p>',
            controller: 'MainCtrl as ctrl'
        });
        $routeProvider.otherwise({
            redirectTo: '/'
        })
    }])
    .factory('TestService', ['$http',
        function($http) {
            var self = this;
            self.todo = 0;

            // TODO: implement signature API
            var hash = function () {
                return "faferouaouvekarueiu";
            };
            return {
                inc: function() { self.todo += 1; },
                todo: function() {return self.todo;}
            }
    }])
    .controller('MainCtrl', ['TestService', '$log', '$location', '$route',
        function (TestService, $log, $location, $route) {
            var self = this;
            self.value = 99;
            self.svcVal = TestService.todo();
            $log.info('Test Controller loaded');

            self.newPath = null;

            self.test = function() {
                self.value += 1;
                TestService.inc();
                self.svcVal = TestService.todo();
            };

            self.first = function() {
                $location.path('/first');
            };
            self.second = function() {
                $location.path('/second');
            };
            self.path = function() {
                $log.info("Current: " + $route.current.locals.$template);
                $location.path(self.newPath);
            }

        }]);
