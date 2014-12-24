gyfp.directive('LoadingCover', function() {
    return {
        templateUrl: '/js/partials/loading-cover.html',
        restrict: 'EA',
        scope: {
            'loading': '@'
        }
    };
});