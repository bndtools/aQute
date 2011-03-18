package aQute.metatype.components;

import java.io.*;
import java.net.*;
import java.util.*;

import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;

@Component(designate = ServerSocketComponent.Config.class, configurationPolicy = ConfigurationPolicy.require)
public class ServerSocketComponent extends Thread {
	interface Config {
		enum Performance {
			bandwidth, latency, connectionTime
		};

		String message();
		int port();
		
		@Meta.AD(deflt="bandwidth")
		Performance performance();
		
		@Meta.AD(deflt="0")
		int soTimeout();
		
		
		@Meta.AD(deflt="8192")
		int receiveBufferSize();
	}

	Config config;
	ServerSocket server;
	LogService log;

	@Activate
	void activate(Map<String, Object> props) {
		config = Configurable.createConfigurable(Config.class, props);
		System.out.println("Hi: " + config.message());
		start();
	}

	@Deactivate
	void deactivate() {
		interrupt();
		try {
			server.close();
		} catch (IOException e) {
			// ignore
		}
		System.out.println("Bye: " + config.message());
	}

	public void run() {
		try {
			server = new ServerSocket(config.port());
			server.setReceiveBufferSize(config.receiveBufferSize());
			server.setSoTimeout(config.soTimeout());
			
			switch (config.performance()) {
			case bandwidth:
				server.setPerformancePreferences(0, 1, 2);
				break;
			case latency:
				server.setPerformancePreferences(2, 1, 0);
				break;
			case connectionTime:
				server.setPerformancePreferences(1, 2, 0);
				break;
			}

			while (!isInterrupted()) {
				Socket socket = server.accept();
				socket.getOutputStream().write(
						(config.message() + "\n").getBytes());
				socket.close();
			}
		} catch (Exception e) {
			if (isInterrupted())
				return;
			log.log(LogService.LOG_ERROR, "IO error", e);
		}
	}

	@Reference
	void setLog(LogService log) {
		this.log = log;
	}
}
