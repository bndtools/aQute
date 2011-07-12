package org.example;

import java.awt.*;

import javax.swing.*;

import org.apache.commons.collections15.*;
import org.osgi.framework.*;
import org.osgi.util.tracker.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.component.Component;

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.tg.*;
import com.tinkerpop.blueprints.pgm.oupls.jung.*;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.*;

@Component
public class ExampleComponent {
	ServiceTracker services;
	BundleTracker bundles;
	
	@Activate
	void activate() throws InvalidSyntaxException {
		final TinkerGraph tinkerGraph = new TinkerGraph();
		
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		final Vertex home = tinkerGraph.addVertex(null);
		
		services= new ServiceTracker(context, context.createFilter("(objectclass=*)"), null) {
			public Object addingService(ServiceReference ref) {
				Vertex service = tinkerGraph.addVertex(null);
				service.setProperty("ref", ref);
				tinkerGraph.addVertex( service);
				tinkerGraph.addEdge(null, home, service, "---");
				return service;
			}
		
			public void removedService(ServiceReference ref, Object s) {
				tinkerGraph.removeVertex((Vertex) s);
			}
		};
		bundles = new BundleTracker(context, -1, null){
			
		};
		services.open();

		final GraphJung graph = new GraphJung(tinkerGraph);
		Layout<Vertex, Edge> layout = new CircleLayout<Vertex, Edge>(graph);
		layout.setSize(new Dimension(1000, 1000));
		BasicVisualizationServer<Vertex, Edge> viz = new BasicVisualizationServer<Vertex, Edge>(
				layout);
		viz.setPreferredSize(new Dimension(350, 350));

		Transformer<Vertex, String> vertexLabelTransformer = new Transformer<Vertex, String>() {
			public String transform(Vertex vertex) {
				return (String) ((ServiceReference)vertex.getProperty("ref")).getProperty("objectclass");
			}
		};

		Transformer<Edge, String> edgeLabelTransformer = new Transformer<Edge, String>() {
			public String transform(Edge edge) {
				return edge.getLabel();
			}
		};

		viz.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer);
		viz.getRenderContext()
				.setVertexLabelTransformer(vertexLabelTransformer);

		JFrame frame = new JFrame("TinkerPop");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(viz);
		frame.pack();
		frame.setVisible(true);

	}
}