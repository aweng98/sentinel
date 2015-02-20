angular.module('testApp', ['ngRoute'])

    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/', {
            template: '<p>TODO: create a default page</p>'
        }).when('/first', {
            templateUrl: '/ui/first.html',
            controller: 'MainCtrl as ctrl'
        }).when('/second', {
            templateUrl: '/ui/second.html',
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
    .controller('MainCtrl', ['TestService', '$log', '$location',
        function (TestService, $log, $location) {
            var self = this;
            self.value = 99;
            self.svcVal = TestService.todo();
            $log.info('Test Controller loaded');


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
            }


        }]);
