/**
 * Created by john on 6/07/16.
 */
'use strict';

var app = angular.module('chooser', ['ngSanitize', 'ui.select']);

// TODO - not happy with using $scope and vm as scopes for attaching variables, should choose one or the other.
function ChooserCtrl($scope, $http, $timeout, $interval) {
    var vm = this;
    var href = window.location.href;
    $scope.geek = href.substring(href.lastIndexOf("/")+1, href.length);
    $scope.loadGeeks = function() {
        var httpRequest = $http({
            method: 'GET',
            url: '/json/geeks'
        }).success(function(data, status) {
            vm.geeks = data["geeks"];
            vm.players = vm.geeks.filter(function(geek) { return geek.username == $scope.geek });
        });
    };
    $scope.loadBaseGames = function() {
        var httpRequest = $http({
            method: 'GET',
            url: '/json/games?q=owned,' + $scope.geek
        }).success(function(data, status) {
            vm.baseGames = data["games"];
        });
    };
    vm.getPlayers = function() {
        return vm.players.map(function(geek) { return geek.username });
    };
    vm.baseOptions = [
        { "name": "Owned by any player"},
        { "name": "Any game at all"}
    ];
    vm.baseOption = {};
    vm.evaluationOptions = [
        { "name": "Highest Total Rating"},
        { "name": "Highest Minimum Rating"},
        { "name": "Total Plays"},
        { "name": "Most Want to Plays"}
    ];
    vm.evaluationFunction = {};
    $scope.loadGeeks();
    $scope.loadBaseGames();
}

app.controller('ChooserCtrl', ChooserCtrl);