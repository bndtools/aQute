# Constants
SWIDTH 			=  20
BHEIGHT 		=  50
HEADER 			=  40
COLUMN 			= 120
J 				=   4
root 			= "/system/console"
skipServices 	= []
top 			= 0


bdef =
	register: 	{ dx: 40, 	dy: 10 }
	get: 		{ dx: 40, 	dy: 10 }
	text: 		{ dx: -35,	dy: -4 }
	listening: 	{ dx: 40, 	dy: 10 }
	components: { dx: -32, 	dy: 7 }
	log: 		{ dx: 32, 	dy: -7 }
	height: 	30
	width: 		80

sdef =
	register: 	{ dx: -15, 	dy:0}
	get: 		{ dx:15, 	dy:0}
	texteven: 	{ dx: 0, 	dy: -9 }
	textodd: 	{ dx: 0, 	dy:9+8 }
	listening: 	{ dx: 0, 	dy:0 },
	height: 	20,
	width: 		20

#
# Drag methods
# 




dragstart = (d, i) ->

dragmove = (d, i) ->
	top -= d3.event.dy
	top = 0	if top < 0
	svg.attr "viewBox", "0 " + top + " 1500 5000"

dragend = (d, i) ->

#
# Wire - Represents a wire from a bundle to a service
# 
class Wire 
	d: ""
	move: (x, y) ->
		@d += "M" + x + "," + y
		@x = x
		@y = y

	line: (x, y) ->
		@d += "L" + x + "," + y
		@x = x
		@y = y
		this

	curve: (cx, cy, x, y) ->
		@d += "Q" + cx + "," + cy + " " + x + "," + y
		@x = x
		@y = y
		this

	split: (x1, y1, x2, y2, n) ->
		dx = x2 - x1
		dy = y2 - y1
		l = Math.sqrt(dx * dx + dy * dy)
		ddx = n * dx / l
		ddy = n * dy / l
		x: ddx
		y: ddy

	join: (p1, p2, n) ->
		from = @split(@x, @y, p1.x, p1.y, n)
		to = @split(p1.x, p1.y, p2.x, p2.y, n)
		@line p1.x - from.x, p1.y - from.y
		@line p1.x + to.x, p1.y + to.y

	h: (x) ->
		@d += "L" + x + "," + @y
		@x = x
		this

	v: (y) ->
		@d += "L" + @x + "," + y
		@y = y
		this


#
# Service life cycle methods
#

initService = (g) ->
	g.attr "transform", "translate(0,0)"
	g.append("a")
		.attr("xlink:href", (d) -> root + "/services" )
		.append("use")
			.attr "xlink:href", "#service"
		
	g.append("title")
		.text (d) -> d.name

	g.append("text")
		.attr("style", "text-anchor:middle;")
		.text (d) -> d.shortName

	g.call drag
	
setService = (g) ->
	g.select("text")
		.attr("dx", (d) -> (if d.column % 2 is 0 then sdef.texteven.dx else sdef.textodd.dx))
		.attr "dy", (d) -> (if d.column % 2 is 0 then sdef.texteven.dy else sdef.textodd.dy)

	g.transition().attr "transform", (d) -> "translate(" + (d.x) + "," + (d.y) + ")"

	g.select("use").attr "class", (d) ->
		return "absent"	if d.registering.length is 0
		return "orphan"	if d.getting.length is 0
		"normal"

	g.select("title")
		.text (d) -> d.name + "\n" + d.ids
	
	g.onclick( (e) -> alert "hello" )
#
# Components
#

initComponent = (d) ->
	d.append("a")
		.attr("xlink:href", (d) -> root + "/components/" + d.id )
		.append("use")
			.attr "xlink:href", (d) ->"#led"

	d.append("title").text (d) -> d.name

setComponent = (d) ->
	d.select("use").attr("class", (d) -> if ( d.unsatisfied) then "ledred" else "ledgreen")
		.attr "transform", (d) -> "translate(" + (bdef.components.dx + d.index * 10) + "," + (bdef.components.dy) + ")"

#
# Bundles
# 

initBundle = (g) ->
	g.attr "transform", "translate(0,0)"
		
	g.append("title")
		.text (d) -> d.bsn

	g.append("a")
		.attr("xlink:href", (d) -> root + "/bundles/" + d.id)
		.append("use")
			.attr "xlink:href", (d) -> "#bundle"

	g.append("text")
		.attr("class", "title")
		.attr("x", 0)
		.attr("y", 0)
		.attr("dx", bdef.text.dx)
		.attr("dy", bdef.text.dy)
		.text (d) -> d.name

	g.append("a")
		.attr("xlink:href", (d) -> root + "/logs")
		.append("use")
			.attr("xlink:href", (d) -> "#log" )
			.attr("led", "log")
			.attr("transform", (d) -> "translate(" + (bdef.log.dx) + "," + (bdef.log.dy) + ")")
			.append "title"


setBundle = (bs) ->
    bs.transition()
    	.attr "transform", (d) -> "translate(" + (d.x) + "," + (d.y) + ")"

    bs.select("use")
    	.attr "class", (d) -> d.state

    cs = bs.selectAll("g.led").data( ((d) -> d.components), (d) -> d.id )
    
    cs.enter().append("g")
    	.attr("class", "led")
    	.call initComponent
    	
    cs.exit().remove()
    cs.call setComponent
    
    bs.select("use[led=log]")
    	.attr("style", (d) ->  if d.errors or d.log then return "visibility:visible;" else return "visibility:hidden;" ).select("title")
    	.text (d) -> d.log

#
# Port concept
#
port = (anchor, delta) ->
	p =
		x: anchor.x + delta.dx
		y: anchor.y + delta.dy
	
#
# Wires
#
initWire = (selection) ->
	selection.attr "class", "wire"
	
setWire = (wires) ->
	wires.attr "d", (d) -> d.d
	

#
# Main program. called when we have an update
#
	
update = (json) ->
	return	unless json?
	
	bundles = json.bundles
	services = json.services
	root = json.root
	cw = 40
	ch = 40
	y = 0
	wires = []
	
	for i of bundles
		bundle = bundles[i]
		bundle.x = bdef.width / 2 + 4
		bundle.y = HEADER + ch * bundle.row
	xx = 0
	yy = 0
	
	for i of services
		icon = services[i]
		icon.x = COLUMN + icon.column * cw
		icon.y = HEADER + icon.row * ch - 10
		l = []
		
		for ref of icon.registering
			bundle = bundles[icon.registering[ref]]
			w = new Wire()
			r0 = port(bundle, bdef.register)
			r1 = port(icon, sdef.register)
			w.move r0.x, r0.y
			w.join
				x: r1.x
				y: r0.y
			, r1, J
			w.join r1, icon, J
			w.line icon.x, icon.y
			w.id = "r" + icon.name + "-" + bundle.id
			wires.push w
			
		for ref of icon.getting
			bundle = bundles[icon.getting[ref]]
			get0 = port(bundle, bdef.get)
			get1 = port(icon, sdef.get)
			w = new Wire()
			w.move get0.x, get0.y
			w.join
				x: get1.x
				y: get0.y
			, get1, J
			w.join get1, icon, J
			w.line icon.x, icon.y
			w.id = "g" + icon.name + "-" + bundle.id
			wires.push w
			
		for ref of icon.listening
			bundle = bundles[icon.listening[ref]]
			l0 = port(bundle, bdef.listening)
			l1 = port(icon, sdef.listening)
			w = new Wire()
			w.move l0.x, l0.y
			w.join
				x: l1.x
				y: l0.y
			, l1, J
			w.line icon.x, icon.y
			w.id = "l" + icon.name + "-" + bundle.id
			wires.push w
	
			
	ws = svg.select("#wiring")
		.selectAll("path.wire")
		.data(wires, (d) -> d.id )
		
	ws.enter().append("path")
		.attr("class", "wire")
		.call initWire
		
	ws.exit().remove()
	ws.call setWire
	
	bs = svg.selectAll("g.bundle")
		.data(bundles, (d) -> d.id )
		
	bs.enter()
		.append("g")
			.attr("class", "bundle")
			.call initBundle
			
	bs.exit().remove()
	bs.call setBundle
	
	services = svg.selectAll("g.service")
		.data(services, (d) -> d.name )
		
	services.exit().remove()
	services.enter().append("g").attr("class", "service").call initService
	services.call setService
	
redraw = ->
	suffix = ""
	suffix = "?ignore=" + skipServices.join("&ignore=")	if skipServices
	d3.json "xray/state.json" + suffix, update

window.xray = this;
	
window.xray.repeat = ->
	redraw()
	setTimeout "window.xray.repeat()", 5000

#
# Initialization
#

svg = d3.select("#state")
drag = d3.behavior.drag().on("drag").on("dragstart", dragstart).on("drag", dragmove).on("dragend", dragend)
svg.call drag
repeat()
