package aQute.jsite.main;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import aQute.jsite.model.*;
import aQute.lib.getopt.*;
import aQute.lib.io.*;
import aQute.libg.reporter.*;

public class JSite extends ReporterAdapter {
	PrintStream		out		= System.out;
	PrintStream		err		= System.err;
	File			base	= new File(System.getProperty("user.dir"));
	options			options;
	DocumentBuilder	db;

	interface options extends Options {
		boolean minify();

		boolean trace();

		String output();

		boolean exceptions();
	}

	/**
	 * mrd --template x.html files...
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		JSite md = new JSite();
		CommandLine cl = new CommandLine(md);

		String s = cl.execute(md, "jsite", Arrays.asList(args));
		if (s != null)
			System.err.println(s);

		if (md.isOk())
			return;

		System.exit(1);
	}

	public void _jsite(options options) throws Exception {
		db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		this.options = options;
		setTrace(options.trace());
		trace("begin %s %s", options._properties(), options._());

		List<String> arguments = options._();
		try {
			if (arguments.isEmpty()) {
				Formatter f = new Formatter(err);
				options._command().help(f, this);
				f.flush();
			} else {
				String cmd = arguments.remove(0);
				String help = options._command().execute(this, cmd, arguments);
				if (help != null) {
					err.println(help);
				}
			}
		}
		catch (Exception e) {
			error("Failed %s", e);
			if (options.exceptions())
				e.printStackTrace();
		}
		report(System.out);
		trace("done");
	}

	@Arguments(arg = {
			"source", "[target]"
	})
	interface compileOptions extends Options {}

	public void _compile(compileOptions options) throws Exception {
		List<String> spec = options._();
		String sourceSpec = spec.remove(0);
		File source = IO.getFile(base, sourceSpec);
		if (!source.isDirectory()) {
			error("The given source is not a directory: %s", source);
			return;
		}

		String targetSpec = spec.isEmpty() ? "static" : spec.remove(0);
		File target = IO.getFile(base, targetSpec);
		if (!target.exists())
			target.mkdirs();

		if (!target.isDirectory()) {
			error("Could not create target directory: %s", target);
			return;
		}

		Site site = new Site(this, source, target);
		if (!site.prepare())
			return;

		if (!isOk())
			return;

		site.build();
	}
}
