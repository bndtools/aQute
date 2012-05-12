package aQute.impl.diagnostic;

import java.util.*;

public class Graph {
	public List<Link>	links	= new ArrayList<Link>();
	public List<Node>	nodes	= new ArrayList<Node>();

	public static class Node {
		public int		index	= -1;
		public int		x		= -1;
		public int		y		= -1;
		public boolean	fixed;
		Object			data;

		public String	id;
		public String	type;
		public String	name;
		public String	title;
		public String	state;
	}

	public static class Link {
		public String	id;
		public int		source	= -1;
		public int		target	= -1;
		public float	strength;
		public float	distance;
	}

	Node addNode(String id, Object data) {
		Node node = new Node();
		node.index = nodes.size();
		nodes.add(node);
		node.id = id;
		node.data = data;
		return node;
	}

	Link link(Node source, Node target) {
		Link link = new Link();
		links.add(link);
		link.id = source.id + "->" + target.id;
		link.source = source.index;
		link.target = target.index;
		return link;
	}

	List<Node> getNodes() {
		return nodes;
	}

	List<Link> getLinks() {
		return links;
	}

}
