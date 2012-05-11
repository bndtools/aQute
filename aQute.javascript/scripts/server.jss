
var log = service(org.osgi.service.log.LogService)


var hello = function(s,args) {
	return [ 1, 2, 3, 4, 5, 7, 8, args, "abc", { d:1, e:2}]
}
