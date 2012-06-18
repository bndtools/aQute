package aQute.impl.freedns;

import java.net.*;
import java.util.*;

import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import aQute.impl.freedns.Freedns.FreednsOptions;
import aQute.lib.io.*;

@Component(designateFactory = FreednsOptions.class, configurationPolicy = ConfigurationPolicy.require)
public class Freedns {
	interface FreednsOptions {
		String key();

		URL url();

		int period();
	}

	Timer			timer;
	FreednsOptions	options;
	LogService		log;
	TimerTask		timerTask	= new TimerTask() {

									@Override
									public void run() {
										try {
											URL url = options.url();
											if (url == null) {
												if (options.key() == null)
													throw new IllegalArgumentException(
															"No url nor key set in configuration for Freedns");
												url = new URL(
														"http://freedns.afraid.org/dynamic/update.php?"
																+ options.key());
											}

											String s = IO.collect(url
													.openStream());
											log.log(LogService.LOG_INFO, s);

										} catch (Exception e) {
											throw new RuntimeException(e);
										}
									}

								};

	void activate(Map<String, Object> map) {
		options = Configurable.createConfigurable(FreednsOptions.class, map);
		timer.schedule(timerTask, 100,
				options.period() <= 0 ? 60000 : options.period() + 60000);
	}

	void deactivate() {
		timerTask.cancel();
	}

	@Reference
	void setLog(LogService log) {
		this.log = log;
	}

	@Reference
	void setTimer(Timer timer) {
		this.timer = timer;
	}
}