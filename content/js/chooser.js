/**
 * Created by john on 6/07/16.
 */
'use strict';

var app = angular.module('chooser', ['ngSanitize', 'ui.select']);

function ChooserCtrl($scope, $http, $timeout, $interval) {
    var vm = this;
    $scope.data = [];
    var href = window.location.href;
    $scope.geek = href.substring(href.lastIndexOf("/")+1, href.length);
    $scope.sortType = 'date';
    $scope.sortReverse = false;
    $scope.loadGeeks = function() {
        var httpRequest = $http({
            method: 'GET',
            url: '/json/geeks'
        }).success(function(data, status) {
            vm.geeks = data["geeks"];
            vm.players = vm.geeks.filter(function(geek) { return geek.username == $scope.geek });
        });
    };
    vm.getPlayers = function() {
        return vm.players.map(function(geek) { return geek.username });
    };
    vm.baseOptions = [
        { "name": "Owned by any player"},
        { "name": "Any game at all"}
    ];
    vm.baseGames = {};
    vm.evaluationOptions = [
        { "name": "Highest Total Rating"},
        { "name": "Highest Minimum Rating"},
        { "name": "Total Plays"}
    ];
    vm.evaluationFunction = {};
    $scope.loadGeeks();
}

app.controller('ChooserCtrl', ChooserCtrl);