/**
 * Created by marco on 6/18/15.
 */

describe('Sentinel Controller', function () {

    // Instantiate a new Sentinel App module
    beforeEach(module('sentinelApp'));

    // the main controller
    var controller, mockServer, $loc;

    beforeEach(inject(function ($controller, $httpBackend, $location) {
        mockServer = $httpBackend;
        $loc = $location;
        controller = $controller("SentinelCtrl");
    }));

    it('should start without an authenticated user', function () {
        expect(controller.authenticated()).toBeFalsy();
    });

    it('should reject a random user', function () {

        user = {
            username: 'foo',
            password: 'bar'
        };
        mockServer.expectPOST('/login', user).respond(401);
        controller.user = user;

        controller.login();
        mockServer.flush();
        expect(controller.authenticated()).toBeFalsy();
    });

    it('should authenticate a valid user', function () {
        user = {
            username: 'test',
            password: 'valid'
        };
        controller.user = user;
        mockServer.expectPOST('/login', user).respond(200, {
            first: 'Test',
            credentials: {
                username: 'test',
                api_key: 'foobar-dead-beef'
            }
        });
        controller.login();
        mockServer.flush();
        expect(controller.authenticated()).toBeTruthy();
        expect($loc.path()).toEqual('/listUsers');
        expect(controller.user.apiKey).toEqual('foobar-dead-beef');
    });

    afterEach(function() {
        mockServer.verifyNoOutstandingExpectation();
        mockServer.verifyNoOutstandingRequest();
    });
});