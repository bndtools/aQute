#
# Module initialize
#

window.JPM = angular.module( 'jpm', ['ngResource'] );

PAGE_SIZE = 3

rp = ($routeProvider) -> $routeProvider.
       when('/program/:bsn',{ templateUrl: '/jpm/program.htm', 	controller: ProgramCtl }).
       when('/program',	  	{ templateUrl: '/jpm/search.htm', 	controller: SearchCtl }).
       otherwise( 		    { redirectTo: '/notfound' } )
       
window.JPM.config( [ '$routeProvider', rp ])


Program = undefined

window.JPMCtl = ($scope, $resource, $location, $routeParams ) ->
    Program = $resource('/rest/program/:bsn',{}, {
      'get': {method:'GET', params: {}},
      'query': {method: 'GET', params:{filter:@filter,start:@start,limit:3}, isArray:true}
    })

    
window.SearchCtl = ($scope, $location, $routeParams ) ->
    $scope.start 	= $routeParams.start;
    $scope.bsn   	= $routeParams.bsn;
    $scope.count   	= 0;
    
    $scope.search 	= -> $scope.start  = 0; search();
    $scope.next 	= -> $scope.start  = Number($scope.start) + 3; search();
    $scope.prev 	= -> if $scope.start >= 3
        $scope.start = Number($scope.start)-3; 
        search() 
    $scope.canSearch= -> $scope.bsn
    search 			= -> 
        $scope.start = 0 unless 0 <= $scope.start <= 100000;
        $scope.programs = Program.query({filter:$scope.bsn,start:$scope.start})
        $location.search("bsn=#{$scope.bsn}&start=#{$scope.start}")
    
    if ( $scope.bsn )
        search()

window.ProgramCtl = ($scope, $location, $routeParams ) ->
    $scope.program = Program.get( $routeParams )
    $scope.qualifier = (r) -> if r.master then '' else '.' + r.qualifier
    $scope.masters = ->	if ($scope.program) then i for i in $scope.program.revisions when i.master else []
    $scope.staging = -> if ($scope.program) then i for i in $scope.program.revisions when !i.master else [] 



