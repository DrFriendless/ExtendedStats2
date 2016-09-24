var app = angular.module('collection', []);

function CollectionCtrl($scope, $http) {
    $scope.data = [];
    var href = window.location.href;
    $scope.geek = href.substring(href.lastIndexOf("/")+1, href.length);
    $scope.sortType = 'date';
    $scope.sortReverse = false;
    $scope.loadCollection = function() {
        var httpRequest = $http({
            method: 'GET',
            url: '/json/geekgames/' + $scope.geek + '?q=all'
        }).success(function(data, status) {
            $scope.data = data;
        });
    };
    $scope.loadCollection();
}

app.controller('CollectionCtrl', CollectionCtrl);
