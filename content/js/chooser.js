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
        var players = vm.getPlayers();
        if (players.length == 0) return;
        var components = [];
        var index = 0;
        var i;
        for (i=0; i<players.length; i++) {
            components[index++] = "owned";
            components[index++]= players[i];
            if (i > 0) components[index++] = "or";
        }
        var httpRequest = $http({
            method: 'GET',
            url: '/json/games?q=' + components.join() + ",players," + players.length + ",and,expansions,minus"
        }).success(function(data, status) {
            vm.baseGames = data["games"];
        });
    };
    vm.getPlayers = function() {
        if (typeof vm.players == "undefined") return [];
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
    // when the player list changes, reload the games collection
    // TODO: this should really depend on the baseOption
    $scope.$watch(
        function() { return vm.players; },

        function(newValue, oldValue) {
            $scope.loadBaseGames();
        }
    );
}

app.controller('ChooserCtrl', ChooserCtrl);