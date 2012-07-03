routes 	= ($routeProvider) -> 
     $routeProvider.
       when('/p/:bsn/r/:rev',	{ templateUrl: '/jpm/revision.htm', 	controller: RevisionCtl }).
       when('/p/:bsn',			{ templateUrl: '/jpm/program.htm', 	controller: ProgramCtl }).
       when('/p',	  			{ templateUrl: '/jpm/search.htm', 	controller: SearchCtl }).
       otherwise( 		    	{ redirectTo: '/p' } )


activate = ( $resource, $location, $routeParams ) ->
    Program = $resource('/rest/program/:bsn',{}, {
      'get': {method:'GET', params: {}},
      'query': {method: 'GET', params:{query:@query,start:@start,limit:PAGE_SIZE}, isArray:true}
    })
    
angular.module( 'jpm', ['ngResource'] ).config(routes).run( activate )
