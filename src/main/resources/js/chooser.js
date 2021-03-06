/**
 * Created by john on 6/07/16.
 */
'use strict';

var app = angular.module('chooser', ['ngSanitize', 'ui.select']);

// TODO - not happy with using $scope and vm as scopes for attaching variables, should choose one or the other.
function ChooserCtrl($scope, $http, $timeout, $interval) {
    var vm = this;
    var href = window.location.href;
    vm.geek = href.substring(href.lastIndexOf("/")+1, href.length);
    vm.loadGeeks = function() {
        var httpRequest = $http({
            method: 'GET',
            url: '/json/geeks'
        }).success(function(data, status) {
            vm.geeks = data["geeks"];
            vm.players = [];
        });
    };
    vm.getPlayers = function() {
        if (typeof vm.players == "undefined") return [];
        return vm.players;
    };
    vm.baseGamesURL = function() {
        var players = vm.getPlayers();
        if (players.length == 0) return null;
        var components = [];
        var index = 0;
        var i;
        for (i=0; i<players.length; i++) {
            components[index++] = vm.baseOption.key;
            components[index++]= players[i];
            if (i > 0) components[index++] = "or";
        }
        return '/json/games?q=' + components.join() + ',expansions,minus'
    };

    vm.loadBestGames = function() {
        var url = vm.baseGamesURL();
        if (url == null) return;
        var more = [];
        var index = 0;
        more[index++] = "players";
        more[index++] = vm.numPlayers;
        more[index++] = "and";
        var i;
        for (i=0; i<vm.players.length; i++) {
            more[index++] = "annotate";
            more[index++]= vm.players[i];
            more[index++] = "playsAnnotate";
            more[index++]= vm.players[i];
        }
        more[index++] = "score";
        more[index++] = vm.evaluationFunction.key;
        url = url + "," + more.join();
        var httpRequest = $http({
            method: 'GET',
            url: url
        }).success(function(data, status) {
            vm.bestGames = data["games"];
            vm.bestGameCount = data["count"];
        });
    };

    vm.baseOptions = [
        { "name": "Owned by any player", "key": "owned"},
        { "name": "Someone wants to buy", "key": "wanttobuy"},
        { "name": "Someone wants to play", "key": "wanttoplay"},
        { "name": "Everyone wants to play", "key": "allwanttoplay"},
        { "name": "At least two want to...", "key": "twowantto"},
        { "name": "At least two want to and someone owns it", "key": "ownedtwowantto"},
    ];
    vm.baseOption = {};
    vm.evaluationOptions = [
        { "name": "Highest Total Rating", "key": "totalRating"},
        { "name": "Highest Minimum Rating", "key": "minimumRating"},
        { "name": "Total Plays", "key": "totalPlays"},
        { "name": "Most Want to Plays", "key": "wantToPlay"},
        { "name": "Most Want to Plays or Buys or Trade", "key": "wantToPlayBuyTrade"}
    ];
    vm.evaluationFunction = {};
    vm.numPlayers = 1;

    vm.loadGeeks();
    vm.loadBestGames();
    // when the player list changes, reload the games collection
    $scope.$watch(
        function() { return vm.players; },

        function(newValue, oldValue) {
            if (typeof newValue == "undefined") return;
            vm.numPlayers = newValue.length;
            vm.loadBestGames();
        }
    );
    $scope.$watch(
        function() { return vm.evaluationFunction; },

        function(newValue, oldValue) {
            vm.loadBestGames();
        }
    );
    $scope.$watch(
        function() { return vm.numPlayers; },

        function(newValue, oldValue) {
            vm.loadBestGames();
        }
    );
    $scope.$watch(
        function() { return vm.baseOption; },

        function(newValue, oldValue) {
            vm.loadBestGames();
        }
    );
    $scope.validRating = function(v,i,a) {
        return v.rating > 0;
    };
    $scope.owned = function(geek) { return geek.flags.indexOf('owned') >= 0 };
    $scope.wanttoplay = function(geek) { return geek.flags.indexOf('wtp') >= 0 };
    $scope.wanttobuy = function(geek) { return geek.flags.indexOf('wtb') >= 0 };
    $scope.want = function(geek) { return geek.flags.indexOf('want') >= 0 };
}

app.controller('ChooserCtrl', ChooserCtrl);