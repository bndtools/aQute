(function(d3) {
	var SWIDTH = 20;
	var BHEIGHT = 50;
	var HEADER = 40;
	var COLUMN = 120;
	var J = 4;
	var root = "/system/console";
	var skipServices = []
	
	var bdef = {
			register: 	{ dx: 40, dy: 10 },
			get: 		{ dx: 40, dy: 10 },
			text: 		{ dx: -35, dy:-4 },
			listening: 	{ dx: 40, dy: 10 },
			components: { dx: -32, dy: 7 },
			log: 		{ dx: 32, dy: -7 },
			height: 	30,
			width: 		80
	}
	var sdef = {
			register: 	{ dx:-15, dy:0},
			get: 		{ dx:15, dy:0},
			texteven: 		{ dx: 0, dy:-9 },
			textodd: 		{ dx: 0, dy:9+8 },
			listening: 	{ dx: 0, dy:0 },
			height: 20,
			width: 20
	}

	var svg = d3.select("#state");

	/**
	 * Dragging services to take them away
	 */
	var drag = d3.behavior.drag().on("drag")
	    .on("dragstart", dragstart)
	    .on("drag", dragmove)
	    .on("dragend", dragend);
	
	var top = 0;
	
	function dragstart(d, i) {
	}
	
	function dragmove(d, i) {
		top -= d3.event.dy;
		if (top < 0)
			top = 0;
		svg.attr("viewBox", "0 " + top + " 1500 5000")
	}
	
	function dragend(d, i) {
	}
	
	svg.call(drag)
	
	/**
	Wire class
	*/
	
	function Wire() {
		this.d = "";
	
		this.move = function(x,y) {
			this.d += "M" + x + "," + y;
			this.x = x;
			this.y = y;
		}
		
		this.line = function(x,y) {
			this.d += "L" + x + "," + y;		
			this.x = x;
			this.y = y;
			return this;
		}
		
		this.curve = function(cx,cy,x,y) {				
			this.d += "Q" + cx + "," + cy+ " " + x + "," + y;
			this.x = x;
			this.y = y;
			return this;
		}

		this.split = function( x1, y1, x2, y2, n) {
			var dx = x2-x1;
			var dy = y2-y1;
			var l = Math.sqrt(dx*dx + dy*dy);
			
			var ddx = n * dx / l;
			var ddy = n * dy / l;
			return { x: ddx, y: ddy};
		}
		
		this.join  = function( p1, p2, n) {
            var from = this.split(this.x, this.y, p1.x, p1.y, n);				
            var to = this.split(p1.x, p1.y, p2.x, p2.y, n);
            this.line(p1.x-from.x,p1.y-from.y);
            this.line(p1.x+to.x,p1.y+to.y);
            //this.curve(p1.x,p1.y,p1.x+to.x,p1.y+to.y);
		}

		this.h = function(x) {				
			this.d += "L" + x + "," + this.y;
			this.x = x;
			return this;
		}

		this.v = function(y) {				
			this.d += "L" + this.x + "," + y;
			this.y = y;
			return this;
		}
	}
	
	
	
	/**
	Initialize the services
	*/
	function initService(g) {
		g.attr("transform","translate(0,0)")
		g.append("a")
			.attr("xlink:href", function(d) {return root + "/services"; })
			.append("use")
				.attr("xlink:href", "#service");
		g.append("title")
			.text(function(d) { return d.name; });
		g.append("text")
			.attr("style","text-anchor:middle;")
			.text(function(d) { return d.shortName; });
		g.call(drag)
	}
	
	function setService(g) {
		g.select("text")
			.attr("dx", function(d) { return d.column %2 == 0 ? sdef.texteven.dx : sdef.textodd.dx; })
			.attr("dy", function(d) { return d.column %2 == 0 ? sdef.texteven.dy : sdef.textodd.dy;})
		
		g.transition().attr("transform", function(d) {
			return "translate(" + (d.x) + "," + (d.y) + ")";
		});
		g.select("use")
			.attr("class", function(d) {
				if ( d.registering.length == 0)
					return "absent";
				if ( d.getting.length == 0)
					return "orphan";
				return "normal";
			});
	}
	
	
	function initComponent(d) {
		d.append("a")
			.attr("xlink:href", function(d) { return root + "/components/"+d.id}) 
			.append("use")	
				.attr("xlink:href", function(d) { return "#led";});
		d.append("title").text(function(d) {return d.name; });
	}
	
	function setComponent(d) {
		d.select("use")	
			.attr("class", function(d) {
				if ( d.unsatisfied)
					return "ledred";
				return "ledgreen";
				
			})
			.attr("transform", function(d) {
				return "translate(" + (bdef.components.dx + d.index*10) + "," + (bdef.components.dy) + ")";
			});
	}
	
	/*
		Initialize the bundle shape
	*/
	function initBundle(g) {
		g.attr("transform", "translate(0,0)");
		g.append("title").text(function(d) {return d.bsn })
		g.append("a")
			.attr("xlink:href", function(d) { return root + "/bundles/"+ d.id;})
			.append("use")
				.attr("xlink:href", function(d) { return "#bundle";})
				;
		
		g.append("text")
			.attr("class", "title")
			.attr("x", 0)
			.attr("y", 0)
			.attr("dx", bdef.text.dx)
			.attr("dy", bdef.text.dy)
			.text(function(d) {return d.name;})
			
		g.append("a")
			.attr("xlink:href", function(d) {return root + "/logs"})
			.append("use")
				.attr("xlink:href", function(d) { return "#log";})
				.attr("led", "log")
				.attr("transform", function(d) {
					return "translate(" + (bdef.log.dx) + "," + (bdef.log.dy) + ")";
				})
				.append("title");
	}
	
	/**
	  Set the bundle state and services
	*/
	
	function setBundle(bs) {
		bs.transition().attr("transform", function(d) {
				return "translate(" + (d.x) + "," + (d.y) + ")";
			});
		bs.select("use").attr("class", function(d) {return d.state;});
		
		var cs = bs.selectAll("g.led").data( function(d) { return d.components; }, function(d) { return d.id; })
		cs.enter().append("g").attr("class", "led").call(initComponent);
		cs.exit().remove();

		cs.call(setComponent)
		
		bs.select("use[led=log]")
			.attr("style", function(d){ 
				if (d.errors || d.log) 
					return "visibility:visible;"; 
				return "visibility:hidden;";
			})
			.select("title").text(function(d) { return d.log; })
	}
	

	function port(anchor, delta ) {
		var p =  { x:anchor.x+delta.dx, y:anchor.y+delta.dy };
		
		return p;
	}		
	
	function initWire(selection) {
		selection
		.attr("class", "wire")
		//.attr("shape-rendering", "crispEdges")
	}
	
	function setWire(wires) {			
		wires
			.attr("d", function(d) { return d.d; })
	}	
	
	function update(json) {
		if ( json == null)
			return;
		
		
		var bundles = json.bundles;
		var services = json.services;
		root = json.root;
		var cw = 40;
		var ch = 40;

		var y = 0;
		var wires = []

		
		for ( var i in bundles ) {
			var bundle=bundles[i]; 
			bundle.x = bdef.width/2+4;
			bundle.y = HEADER + ch*bundle.row;
		}
		
		var xx = 0;
		var yy = 0;
		for ( var i in services ) {
			var icon = services[i]
			icon.x = COLUMN +  icon.column * cw;
			icon.y = HEADER +  icon.row * ch - 10;
			
			var l = []
			for ( var ref in icon.registering ) {
				var bundle = bundles[icon.registering[ref]];
				
				var w = new Wire();
				var r0 = port(bundle,bdef.register);
				var r1 = port(icon,sdef.register); 
					
				w.move(r0.x,r0.y);
				w.join({x:r1.x, y:r0.y}, r1, J);
				w.join(r1, icon, J);
				w.line(icon.x,icon.y);

				w.id = "r"+icon.name + "-" + bundle.id;
				wires.push(w)
			}
			for ( var ref in icon.getting ) {
				var bundle = bundles[icon.getting[ref]];
				var get0  = port(bundle,bdef.get);
				var get1 = port(icon,sdef.get);
				
				var w = new Wire();
				w.move(get0.x, get0.y);
				w.join({x:get1.x,y:get0.y}, get1,J);
				w.join(get1, icon,J);
				w.line(icon.x, icon.y);
				w.id = "g"+icon.name + "-" + bundle.id;
				wires.push(w)
			}
			for ( var ref in icon.listening ) {
				var bundle = bundles[icon.listening[ref]];
				var l0  = port(bundle,bdef.listening);
				var l1 = port(icon,sdef.listening);
				
				var w = new Wire();
				w.move(l0.x, l0.y);
				w.join({x:l1.x,y:l0.y}, l1,J);
				w.line(icon.x, icon.y);
				w.id = "l"+icon.name + "-" + bundle.id;
				wires.push(w)
			}
		}
		
		
		/* Wires */
		var ws = svg.select("#wiring").selectAll("path.wire").data(wires, function(d) { return d.id; })
		ws.enter().append("path").attr("class","wire").call(initWire);
		ws.exit().remove();
		ws.call(setWire);
		
		/* Bundles */
		var bs = svg.selectAll("g.bundle").data(bundles,
				function(d) {
					return d.id;
				})
		bs.enter().append("g").attr("class","bundle").call(initBundle);
		bs.exit().remove();
		bs.call(setBundle);
		
		/* Icons */
		var services = svg.selectAll("g.service").data(services, function(d) {return d.name; })
		services.exit().remove();
		services.enter().append("g").attr("class", "service").call(initService);			
		services.call(setService);
	}
	
	function redraw() {
		var suffix = "";
		if ( skipServices ) {
			suffix = "?ignore=" + skipServices.join("&ignore=");
		}
		d3.json("xray/state.json" + suffix, update);			
	}
	function repeat() {
		redraw();
		setTimeout("repeat()", 5000)
	}
	repeat();
})(d3)
	
