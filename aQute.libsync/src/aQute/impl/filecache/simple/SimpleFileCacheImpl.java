package aQute.impl.filecache.simple;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.osgi.framework.*;
import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.lib.converter.*;
import aQute.lib.io.*;
import aQute.libg.cryptography.*;
import aQute.service.filecache.*;

@Component(designate = SimpleFileCacheImpl.Config.class, configurationPolicy = ConfigurationPolicy.require)
public class SimpleFileCacheImpl implements FileCache {
	static Converter	converter	= new Converter();

	interface Config {
		String cacheDir();

		long size();

		long maxLockTime();

		int reapPeriod();
	}

	Config				config;
	File				cacheDir;
	TimerTask			reaper;
	volatile boolean	stopped	= false;
	Set<String>			locks	= new HashSet<String>();

	// References
	LogService			log;
	Executor			executor;
	Timer				timer;

	@Activate
	void activate(Map<String,Object> p) throws Exception {
		config = converter.convert(Config.class, p);

		if (config.cacheDir() != null)
			cacheDir = IO.getFile(cacheDir.getParentFile(), config.cacheDir());

		if (cacheDir == null) {
			Bundle bundle = FrameworkUtil.getBundle(getClass());
			if (bundle != null)
				cacheDir = bundle.getBundleContext().getDataFile("cache");
		}

		if (cacheDir != null) {
			cacheDir.mkdirs();
			if (!cacheDir.isDirectory())
				throw new RuntimeException("Invalid cache directory: " + cacheDir);
		}

		reaper = new TimerTask() {

			@Override
			public void run() {
				executor.execute(new Runnable() {

					@Override
					public void run() {
						reap();
					}
				});
			}

		};
		timer.schedule(reaper, TimeUnit.HOURS.toMillis(config.reapPeriod() <= 0 ? 4 : config.reapPeriod()));
	}

	public SimpleFileCacheImpl setCacheDir(File cacheDir) {
		this.cacheDir = cacheDir;
		return this;
	}

	@Deactivate
	void deactivate() {
		stopped = true;
		reaper.cancel();
	}

	@Override
	public File get(String name, Callable<InputStream> cb) throws Exception {
		name = encode(name);
		File f = null;
		if (cacheDir != null && cacheDir.isDirectory())
			f = IO.getFile(cacheDir, name);
		else {
			f = File.createTempFile(name, ".cache");
			f.delete();
		}

		int max = 2;
		while (!f.isFile() && max-- > 0) {
			lock(name);
			try {
				//
				// we have a race here, so
				// make sure we're the creator
				//

				if (f.createNewFile()) {
					try {
						IO.copy(cb.call(), f);
						f.setReadOnly();
					}
					catch (Exception e) {
						e.printStackTrace();
						f.delete();
					}
				}
			}
			finally {
				unlock(name);
			}
		}
		f.setLastModified(System.currentTimeMillis());
		return f;
	}

	private void unlock(String name) {
		synchronized (locks) {
			locks.remove(name);
			locks.notifyAll();
		}
	}

	private void lock(String name) {
		long deadline = System.currentTimeMillis() + Math.max(60000, config.maxLockTime());
		synchronized (locks) {
			while (true) {
				if (locks.add(name))
					return;

				if (deadline > System.currentTimeMillis())
					return;

				try {
					wait(5000);
				}
				catch (InterruptedException e) {
					throw new RuntimeException("Interrupted while locked");
				}
			}
		}
	}

	private String encode(String name) throws Exception {
		Digester<SHA1> digester = SHA1.getDigester();
		digester.write(name.getBytes("UTF-8"));
		return digester.digest().asHex();
	}

	@Override
	public long getExpiration(File file) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long setExpiration(File file, long expiration) {
		// TODO Auto-generated method stub
		return 0;
	}

	void reap() {
		SortedSet<File> set = new TreeSet<File>(new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return o1.lastModified() == o2.lastModified() ? 0 : o1.lastModified() > o2.lastModified() ? 1 : -1;
			}

		});
		long size = reap(cacheDir, set);
		for (Iterator<File> i = set.iterator(); i.hasNext();) {
			if (size < config.size() * 1000000)
				break;

			File file = i.next();
			size -= file.length();
			file.setWritable(true);
			file.delete();
			log.log(LogService.LOG_INFO, "deleted cache entry " + file.getAbsolutePath());
		}
	}

	long reap(File dirOrFile, Set<File> set) {
		if (stopped)
			return -1;
		long size = 0;
		if (dirOrFile.isDirectory()) {
			File[] files = dirOrFile.listFiles();
			for (File file : files) {
				if (stopped)
					return -1;
				size += reap(file, set);
			}
			return size;
		} else if (dirOrFile.isFile()) {
			set.add(dirOrFile);
			return dirOrFile.length();
		} else
			return 0;
	}

	@Reference
	void setTimer(Timer timer) {
		this.timer = timer;
	}

	@Reference
	void setLog(LogService log) {
		this.log = log;
	}

	@Reference()
	void setExecutor(Executor executor) {
		this.executor = executor;
	}
}
