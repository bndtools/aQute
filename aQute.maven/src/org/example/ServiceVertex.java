package org.example;

import java.util.*;

import org.osgi.framework.*;

import com.tinkerpop.blueprints.pgm.*;

public class ServiceVertex implements Vertex {
	final ServiceReference ref;
	
	ServiceVertex(ServiceReference ref) {
		this.ref = ref;
	}
	
	@Override
	public Object getId() {
		return ref.getProperty("service.id");
	}

	@Override
	public Object getProperty(String key) {
		return ref.getProperty(key);
	}

	@Override
	public Set<String> getPropertyKeys() {
		return new HashSet<String> (Arrays.asList(ref.getPropertyKeys()));
	}

	@Override
	public Object removeProperty(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProperty(String arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<Edge> getInEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Edge> getInEdges(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Edge> getOutEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Edge> getOutEdges(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
