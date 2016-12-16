'use strict';

var app = angular.module('newgames', ['ngSanitize', 'ui.select']);

function NewGamesCtrl($scope, $http, $timeout, $interval) {
    var vm = this;
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
    vm.dataURL = function() {
        var players = vm.getPlayers();
        if (players.length == 0) return null;
        return '/json/newgames/2005/' + players.join();
    };

    vm.loadData = function() {
        var url = vm.dataURL();
        if (url == null) return;

        $.ajax({
            dataType:'json',
            url: url,
            data: { format: 'json'},
            success: function(data) {
                $(function () {
                    var myChart = Highcharts.chart('graph', {
                        chart: { type: 'scatter' },
                        title: { text: 'Plays of New Games' },
                        plotOptions: {
                            scatter: {
                                tooltip: {
                                    headerFormat: '<b>{point.key}</b> <br>',
                                    pointFormat: '{point.x: %Y-%m-%d}'
                                }
                            }
                        },
                        series: [
                            {
                                name: "Friendless",
                                data: [{x:1481806801000,y:1,name:"Puerto Rico"}, {x:1481807401000,y:2,name:"San Juan"}]
                            },
                            {
                                name: "Eduardo",
                                data: [{x:1481806831000,y:1,name:"Chess"}, {x:1481807431000,y:2,name:"Checkers"}]
                            }
                         ],
                        xAxis: { type: 'datetime', title: { text: "Start Time"} }
                    });
                });
            },
            type: 'GET'
        });
    };

    vm.loadGeeks();
    vm.loadData();

    $scope.$watch(
        function() { return vm.players; },

        function(newValue, oldValue) {
            if (typeof newValue == "undefined") return;
            vm.numPlayers = newValue.length;
            vm.loadData();
        }
    );    
}

app.controller('NewGamesCtrl', NewGamesCtrl);