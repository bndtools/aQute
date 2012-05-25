package org.example;

import java.net.*;
import java.util.*;

import org.java_websocket.*;
import org.java_websocket.handshake.*;

import aQute.bnd.annotation.component.*;

@Component(immediate=true)
public class ExampleComponent extends WebSocketServer {

	public ExampleComponent() throws UnknownHostException {
		super(new InetSocketAddress(9910));
		System.out.println("Constructor WebSocket " + getAddress().getPort());
		start();
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		System.out.println("Open WebSocket " + conn);
		System.out.println( conn + " entered the room!" );
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println("Close WebSocket " + conn + " " + code + " "
				+ reason);
		this.sendToAll(conn + " has left the room!");
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("Message WebSocket " + conn + " " + message);
		this.sendToAll( message );
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		System.out.println("Error WebSocket " + conn + " " + ex);
		ex.printStackTrace();
	}

	public void sendToAll(String text){
		Set<WebSocket> con = connections();
		synchronized (con) {
			for (WebSocket c : con) {
				try {
					c.send(text);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}