package aqute.eclipse.coffee;


public class RhinoCoffee {
	// static boolean initialized;
	// static Context context;
	// static ScriptableObject scope;
	// static Function compile;
	// static Function eval;
	// static Converter converter = new Converter();
	//
	// static {
	// try {
	// context = Context.enter();
	// context.setOptimizationLevel(9);
	// scope = context.initStandardObjects();
	// String cs = IO.collect(RhinoCoffee.class
	// .getResourceAsStream("coffee-script.js"));
	// context.evaluateString(scope, cs, "coffee-script", 1, null);
	// compile = context
	// .compileFunction(
	// scope,
	// "function (source) { return CoffeeScript.compile(source); }",
	// "x", 1, null);
	// eval = context
	// .compileFunction(
	// scope,
	// "function (source) { return CoffeeScript.eval(source); }",
	// "x", 1, null);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// static public String compile(String source) throws Exception {
	// System.out.println("Compile: " + source);
	// return compile.call(context, scope, null, new Object[] {source}) + "";
	// }
	//
	// static public String exec(String source) throws Exception {
	// Object result = eval.call(context, scope, null, new Object[]{source});
	// StringBuilder sb = new StringBuilder();
	// convert(sb,result,0);
	// return sb.toString();
	// }
	//
	// private static void convert(StringBuilder sb, Object result, int indent)
	// {
	// if ( result instanceof Map) {
	// sb.append("{");
	// String del = "";
	//
	// for ( Map.Entry e : ((Map<Object,Object>)result).entrySet()) {
	// sb.append(del);
	// sb.append(e.getKey());
	// sb.append(": ");
	// convert(sb, e.getValue(), indent+1);
	// del = ", ";
	// }
	// sb.append("}");
	// return;
	// }
	//
	// if ( result instanceof Collection) {
	// sb.append('[');
	// String del = "";
	// for ( Object o : ((Collection)result)) {
	// sb.append(del);
	// convert(sb, o, indent+1);
	// del = ", ";
	// }
	// sb.append(']');
	// return;
	// }
	// if ( result instanceof String ) {
	// String s = ((String)result).replaceAll("\\\\", "\\\\").replaceAll("'",
	// "\\\\'");
	// sb.append("'");
	// sb.append( s);
	//
	// sb.append("'");
	// return;
	// }
	//
	// sb.append(Context.toString(result));
	// }
	//
	// public static void main(String[] args) throws Exception {
	// System.out.println(exec("countdown = (num for num in [10..1] when num %2==0)"));
	// System.out.println(exec(" () -> 1"));
	// }
	//
}
