# main for the JPM search window
# test = [{bsn:"bsn", version:"v", url:"u", text:"t"}]

jpm = angular.module( 'jpm', ['ngResource'] )
 
window.JPMCtrl = ($scope, $http, $resource) ->
    Program = $resource('/rest/program/:bsn',{}, {
                        'get': {method:'GET'},
                        'query': {method: 'GET', isArray:true}
                        })
    $scope.programs = Program.query()
    
