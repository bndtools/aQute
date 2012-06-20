package aqute.eclipse.coffee;

import aQute.libg.command.*;

public class NativeCoffee {
	static final String	NIX_PATH	= "/opt/local/bin:/opt/local/sbin:/Developer/usr/bin:/opt/local/bin:/opt/local/sbin:/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/usr/X11/bin:/usr/local/git/bin";
	static final String	WIN_PATH	= "";

	static public String compile(String source) throws Exception {
		Command command = new Command();
		command.arg("sh", "-c", "coffee -sc").var("PATH", NIX_PATH);
		StringBuilder stdout = new StringBuilder();
		StringBuilder stderr = new StringBuilder();
		int result = command.execute(source, stdout, stderr);
		if (result != 0)
			throw new RuntimeException(result + " : " + stderr + stdout);

		return stdout.toString();
	}

	static public String eval(String source) throws Exception {
		Command command = new Command();
		command.arg("sh", "-c", "node").var("PATH", NIX_PATH);
		StringBuilder stdout = new StringBuilder();
		StringBuilder stderr = new StringBuilder();
		String s = compile("console.log (" + source + ")");
		int result = command.execute(s, stdout, stderr);
		if (result != 0)
			throw new RuntimeException(result + " : " + stderr.toString() + stdout.toString());

		return stdout.toString();
	}

	public static void main(String[] args) throws Exception {
		NativeCoffee n = new NativeCoffee();
		System.out.println("compile " + n.compile("()-> 1"));
		System.out.println("compile " + n.compile("{ a:i, b:i*i } for i in [1..10]"));
		System.out.println("eval " + n.eval("{ a:i, b:i*i } for i in [1..10]"));
	}

}
