'use strict'

angular.module('demo', [])
    .controller('homeController', function($scope, $http) {
        $http.get('/hero/all').
            then(function(response) {
                console.log(response.data);
                $scope.heroes = response.data;
                console.log($scope.heroes);
            });
});