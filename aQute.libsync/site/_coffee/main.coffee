# 
# Main program for the jpm.html page. 


PAGE_SIZE 	= 100				# Default page size
Program 	= undefined		# shared resource manager for programs

#
# Controller for the search fragment
#
SearchCtl = ($scope, $location, $routeParams ) ->
    $scope.start 	= $routeParams.start;
    $scope.query    = "*";
    $scope.count   	= 0;
    
    $scope.search 	= -> $scope.start  = 0; search();
    $scope.next 	= -> $scope.start  = Number($scope.start) + PAGE_SIZE; search();
    $scope.prev 	= -> if $scope.start >= PAGE_SIZE
        $scope.start = Number($scope.start)-PAGE_SIZE; 
        search() 
    $scope.canSearch= -> $scope.query
    search 			= -> 
        $scope.start = 0 unless 0 <= $scope.start <= 100000;
        $scope.programs = Program.query({query:$scope.query,start:$scope.start})
        $location.search("start=#{$scope.start}")
    
    if ( $scope.query )
        search()

#
# Controller for the program fragment
# See jpm/

ProgramCtl = ($scope, $location, $routeParams ) ->
    $scope.program 		= Program.get( $routeParams )
    $scope.qualifier 	= (r) -> if r.master then '' else '.' + r.qualifier
    $scope.type         = (r) -> if r.master then 'master' else 'staged'
    

#
# Controller for the revision fragment
#
RevisionCtl = ($scope, $location, $routeParams ) ->
    $scope.program 		= Program.get( $routeParams )
    $scope.qualifier 	= (r) -> if r.master then '' else '.' + r.qualifier
    $scope.masters 		= ->	if ($scope.program) then i for i in $scope.program.revisions when i.master else []
    $scope.staging 		= -> if ($scope.program) then i for i in $scope.program.revisions when !i.master else [] 
    
        
