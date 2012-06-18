(function() {
  var BHEIGHT, COLUMN, HEADER, J, SWIDTH, Wire, bdef, drag, dragend, dragmove, dragstart, initBundle, initComponent, initService, initWire, port, redraw, root, sdef, setBundle, setComponent, setService, setWire, skipServices, svg, top, update;

  SWIDTH = 20;

  BHEIGHT = 50;

  HEADER = 40;

  COLUMN = 120;

  J = 4;

  root = "/system/console";

  skipServices = [];

  top = 0;

  bdef = {
    register: {
      dx: 40,
      dy: 10
    },
    get: {
      dx: 40,
      dy: 10
    },
    text: {
      dx: -35,
      dy: -4
    },
    listening: {
      dx: 40,
      dy: 10
    },
    components: {
      dx: -32,
      dy: 7
    },
    log: {
      dx: 32,
      dy: -7
    },
    height: 30,
    width: 80
  };

  sdef = {
    register: {
      dx: -15,
      dy: 0
    },
    get: {
      dx: 15,
      dy: 0
    },
    texteven: {
      dx: 0,
      dy: -9
    },
    textodd: {
      dx: 0,
      dy: 9 + 8
    },
    listening: {
      dx: 0,
      dy: 0
    },
    height: 20,
    width: 20
  };

  dragstart = function(d, i) {};

  dragmove = function(d, i) {
    top -= d3.event.dy;
    if (top < 0) top = 0;
    return svg.attr("viewBox", "0 " + top + " 1500 5000");
  };

  dragend = function(d, i) {};

  Wire = (function() {

    function Wire() {}

    Wire.prototype.d = "";

    Wire.prototype.move = function(x, y) {
      this.d += "M" + x + "," + y;
      this.x = x;
      return this.y = y;
    };

    Wire.prototype.line = function(x, y) {
      this.d += "L" + x + "," + y;
      this.x = x;
      this.y = y;
      return this;
    };

    Wire.prototype.curve = function(cx, cy, x, y) {
      this.d += "Q" + cx + "," + cy + " " + x + "," + y;
      this.x = x;
      this.y = y;
      return this;
    };

    Wire.prototype.split = function(x1, y1, x2, y2, n) {
      var ddx, ddy, dx, dy, l;
      dx = x2 - x1;
      dy = y2 - y1;
      l = Math.sqrt(dx * dx + dy * dy);
      ddx = n * dx / l;
      ddy = n * dy / l;
      return {
        x: ddx,
        y: ddy
      };
    };

    Wire.prototype.join = function(p1, p2, n) {
      var from, to;
      from = this.split(this.x, this.y, p1.x, p1.y, n);
      to = this.split(p1.x, p1.y, p2.x, p2.y, n);
      this.line(p1.x - from.x, p1.y - from.y);
      return this.line(p1.x + to.x, p1.y + to.y);
    };

    Wire.prototype.h = function(x) {
      this.d += "L" + x + "," + this.y;
      this.x = x;
      return this;
    };

    Wire.prototype.v = function(y) {
      this.d += "L" + this.x + "," + y;
      this.y = y;
      return this;
    };

    return Wire;

  })();

  initService = function(g) {
    g.attr("transform", "translate(0,0)");
    g.append("a").attr("xlink:href", function(d) {
      return root + "/services";
    }).append("use").attr("xlink:href", "#service");
    g.append("title").text(function(d) {
      return d.name;
    });
    g.append("text").attr("style", "text-anchor:middle;").text(function(d) {
      return d.shortName;
    });
    return g.call(drag);
  };

  setService = function(g) {
    g.select("text").attr("dx", function(d) {
      if (d.column % 2 === 0) {
        return sdef.texteven.dx;
      } else {
        return sdef.textodd.dx;
      }
    }).attr("dy", function(d) {
      if (d.column % 2 === 0) {
        return sdef.texteven.dy;
      } else {
        return sdef.textodd.dy;
      }
    });
    g.transition().attr("transform", function(d) {
      return "translate(" + d.x + "," + d.y + ")";
    });
    return g.select("use").attr("class", function(d) {
      if (d.registering.length === 0) return "absent";
      if (d.getting.length === 0) return "orphan";
      return "normal";
    });
  };

  initComponent = function(d) {
    d.append("a").attr("xlink:href", function(d) {
      return root + "/components/" + d.id;
    }).append("use").attr("xlink:href", function(d) {
      return "#led";
    });
    return d.append("title").text(function(d) {
      return d.name;
    });
  };

  setComponent = function(d) {
    return d.select("use").attr("class", function(d) {
      if (d.unsatisfied) {
        return "ledred";
      } else {
        return "ledgreen";
      }
    }).attr("transform", function(d) {
      return "translate(" + (bdef.components.dx + d.index * 10) + "," + bdef.components.dy + ")";
    });
  };

  initBundle = function(g) {
    g.attr("transform", "translate(0,0)");
    g.append("title").text(function(d) {
      return d.bsn;
    });
    g.append("a").attr("xlink:href", function(d) {
      return root + "/bundles/" + d.id;
    }).append("use").attr("xlink:href", function(d) {
      return "#bundle";
    });
    g.append("text").attr("class", "title").attr("x", 0).attr("y", 0).attr("dx", bdef.text.dx).attr("dy", bdef.text.dy).text(function(d) {
      return d.name;
    });
    return g.append("a").attr("xlink:href", function(d) {
      return root + "/logs";
    }).append("use").attr("xlink:href", function(d) {
      return "#log";
    }).attr("led", "log").attr("transform", function(d) {
      return "translate(" + bdef.log.dx + "," + bdef.log.dy + ")";
    }).append("title");
  };

  setBundle = function(bs) {
    var cs;
    bs.transition().attr("transform", function(d) {
      return "translate(" + d.x + "," + d.y + ")";
    });
    bs.select("use").attr("class", function(d) {
      return d.state;
    });
    cs = bs.selectAll("g.led").data((function(d) {
      return d.components;
    }), function(d) {
      return d.id;
    });
    cs.enter().append("g").attr("class", "led").call(initComponent);
    cs.exit().remove();
    cs.call(setComponent);
    return bs.select("use[led=log]").attr("style", function(d) {
      if (d.errors || d.log) {
        return "visibility:visible;";
      } else {
        return "visibility:hidden;";
      }
    }).select("title").text(function(d) {
      return d.log;
    });
  };

  port = function(anchor, delta) {
    var p;
    return p = {
      x: anchor.x + delta.dx,
      y: anchor.y + delta.dy
    };
  };

  initWire = function(selection) {
    return selection.attr("class", "wire");
  };

  setWire = function(wires) {
    return wires.attr("d", function(d) {
      return d.d;
    });
  };

  update = function(json) {
    var bs, bundle, bundles, ch, cw, get0, get1, i, icon, l, l0, l1, r0, r1, ref, services, w, wires, ws, xx, y, yy;
    if (json == null) return;
    bundles = json.bundles;
    services = json.services;
    root = json.root;
    cw = 40;
    ch = 40;
    y = 0;
    wires = [];
    for (i in bundles) {
      bundle = bundles[i];
      bundle.x = bdef.width / 2 + 4;
      bundle.y = HEADER + ch * bundle.row;
    }
    xx = 0;
    yy = 0;
    for (i in services) {
      icon = services[i];
      icon.x = COLUMN + icon.column * cw;
      icon.y = HEADER + icon.row * ch - 10;
      l = [];
      for (ref in icon.registering) {
        bundle = bundles[icon.registering[ref]];
        w = new Wire();
        r0 = port(bundle, bdef.register);
        r1 = port(icon, sdef.register);
        w.move(r0.x, r0.y);
        w.join({
          x: r1.x,
          y: r0.y
        }, r1, J);
        w.join(r1, icon, J);
        w.line(icon.x, icon.y);
        w.id = "r" + icon.name + "-" + bundle.id;
        wires.push(w);
      }
      for (ref in icon.getting) {
        bundle = bundles[icon.getting[ref]];
        get0 = port(bundle, bdef.get);
        get1 = port(icon, sdef.get);
        w = new Wire();
        w.move(get0.x, get0.y);
        w.join({
          x: get1.x,
          y: get0.y
        }, get1, J);
        w.join(get1, icon, J);
        w.line(icon.x, icon.y);
        w.id = "g" + icon.name + "-" + bundle.id;
        wires.push(w);
      }
      for (ref in icon.listening) {
        bundle = bundles[icon.listening[ref]];
        l0 = port(bundle, bdef.listening);
        l1 = port(icon, sdef.listening);
        w = new Wire();
        w.move(l0.x, l0.y);
        w.join({
          x: l1.x,
          y: l0.y
        }, l1, J);
        w.line(icon.x, icon.y);
        w.id = "l" + icon.name + "-" + bundle.id;
        wires.push(w);
      }
    }
    ws = svg.select("#wiring").selectAll("path.wire").data(wires, function(d) {
      return d.id;
    });
    ws.enter().append("path").attr("class", "wire").call(initWire);
    ws.exit().remove();
    ws.call(setWire);
    bs = svg.selectAll("g.bundle").data(bundles, function(d) {
      return d.id;
    });
    bs.enter().append("g").attr("class", "bundle").call(initBundle);
    bs.exit().remove();
    bs.call(setBundle);
    services = svg.selectAll("g.service").data(services, function(d) {
      return d.name;
    });
    services.exit().remove();
    services.enter().append("g").attr("class", "service").call(initService);
    return services.call(setService);
  };

  redraw = function() {
    var suffix;
    suffix = "";
    if (skipServices) suffix = "?ignore=" + skipServices.join("&ignore=");
    return d3.json("xray/state.json" + suffix, update);
  };

  window.xray = this;

  window.xray.repeat = function() {
    redraw();
    return setTimeout("window.xray.repeat()", 5000);
  };

  svg = d3.select("#state");

  drag = d3.behavior.drag().on("drag").on("dragstart", dragstart).on("drag", dragmove).on("dragend", dragend);

  svg.call(drag);

  repeat();

}).call(this);
