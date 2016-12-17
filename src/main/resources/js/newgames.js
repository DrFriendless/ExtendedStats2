'use strict';

var app = angular.module('newgames', ['ngSanitize', 'ui.select']);
// https://gist.github.com/ollieglass/f6ddd781eeae1d24e391265432297538
var kelly_colors = ['#222222', '#F3C300', '#875692', '#F38400', '#A1CAF1', '#BE0032', '#C2B280', '#848482',
    '#008856', '#E68FAC', '#0067A5', '#F99379', '#604E97', '#F6A600', '#B3446C', '#DCD300', '#882D17', '#8DB600',
    '#654522', '#E25822', '#2B3D26']


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
        return '/json/newgames/2003/' + players.join();
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
                        colors: kelly_colors,
                        title: { text: 'Plays of New Games' },
                        plotOptions: {
                            scatter: {
                                tooltip: {
                                    headerFormat: '<b>{point.key}</b> <br>',
                                    pointFormat: '{point.x: %Y-%m-%d}'
                                }
                            },
                            series: {
                                turboThreshold: 100000
                            }
                        },
                        series: data,
                        xAxis: { type: 'datetime', title: { text: "Start Time"} },
                        yAxis: { title: { text: "Unique Games Played" }}
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