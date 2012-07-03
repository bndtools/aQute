package aQute.jsite.model;

import static java.lang.Math.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import aQute.lib.io.*;
import aQute.lib.json.*;
import aQute.lib.tag.*;
import aQute.libg.command.*;
import aQute.libg.reporter.*;

import com.github.rjeschke.txtmark.*;

/**
 * <pre>
 * </pre>
 * 
 * @author aqute
 */
public class Site {
	static JSONCodec			codec	= new JSONCodec();
	static TransformerFactory	tf		= TransformerFactory.newInstance();
	DocumentBuilder				db;
	XPath						xpath;
	final File					source;
	final File					target;
	final File					receipt;
	final File					siteSpecFile;
	final SiteSpec				siteSpec;
	final Reporter				reporter;

	private Transformer			defaultTemplate;
	private Transformer			unity;
	private Level				rootLevel;
	private File				template;
	private Document			siteDocument;

	public static class SiteSpec {
		public String		name;
		public String		prefix;
		public String		root		= "";
		public String		template	= "_xsl/default.xsl";
		Map<String,Object>	__extra;
	}

	static class Doc {
		File	source;
		String	path;

		Doc(File file) {
			this.source = file;
		}

		String getName() {
			return source.getName();
		}

		File getSourceFile() {
			return source;
		}

		Tag addTo(Tag site) throws IOException {
			Tag tag = new Tag(site, "doc");
			tag.addAttribute("name", getName());
			tag.addAttribute("source", source.getAbsolutePath());
			return tag;
		}

		public void transfer(File target) throws Exception {
			IO.copy(source, target);
		}

		public void setPath(String path) {
			this.path = path;
		}
	}

	class XmlDoc extends Doc {

		XmlDoc(File file) {
			super(file);
		}

		Document getXmlContent() throws Exception {
			FileReader r = null;
			try {
				r = new FileReader(source);
				return db.parse(new InputSource(r));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			finally {
				IO.close(r);
			}
		}

		public void transfer(File target) throws Exception {
			if (defaultTemplate == null) {
				tf.setURIResolver(new URIResolver() {
					public Source resolve(String href, String base) throws TransformerException {
						if (href.equals("site"))
							return new DOMSource(siteDocument);

						return null;
					}
				});
				if (!template.isFile()) {
					reporter.error("Template not found %s", template);
					return;
				}
				defaultTemplate = tf.newTransformer(new StreamSource(template));
				unity = tf.newTransformer();
			}
			Document document = getXmlContent();
			DOMResult result = new DOMResult();

			defaultTemplate.transform(new DOMSource(document), result);

			cleanup(result.getNode());
			unity.transform(new DOMSource(result.getNode()), new StreamResult(target));
		}

		private void cleanup(Node node) throws XPathExpressionException {

			// Cleanup the links, make sure that links to oneself are
			// marked active.
			NodeList list = (NodeList) xpath.evaluate("//a[@href='" + path + "']", node, XPathConstants.NODESET);
			for (int i = 0; i < list.getLength(); i++) {
				Element a = (Element) list.item(i);

				String clazz = a.getAttribute("class");
				if (clazz == null || clazz.isEmpty())
					clazz = "active";
				else
					clazz = clazz + " active";
				a.setAttribute("class", clazz);
			}

		}
	}

	class ExecDoc extends Doc {
		String	name;

		ExecDoc(File file) {
			super(file);
			name = file.getName().substring(0, file.getName().length() - 5);
		}

		String getName() {
			return name;
		}

		public void transfer(File target) throws Exception {
			StringBuilder errors = new StringBuilder();
			String command = IO.collect(source);
			try {
				reporter.trace("executing command %s", command);
				Command cmd = new Command("sh -l");
				cmd.setCwd(source.getParentFile());
				PrintWriter writer = IO.writer(target);
				try {
					int result = cmd.execute(command, writer, errors);
					writer.flush();
					if (result != 0) {
						reporter.error("executing command failed %s %s", command, IO.collect(target) + "\n" + errors);
						target.delete();
					}
				}
				finally {
					writer.close();
				}

			}
			catch (Exception e) {
				reporter.error("executing command failed %s %s", command, e.getMessage());
			}
		}
	}

	static Pattern	MARKER	= Pattern
									.compile("<\\s*p\\s*>\\s*<\\s*([A-Z][A-Z0-9_-]*)\\s*/>\\s*(<\\s*/\\s*p\\s*>\\s*)?");

	class MarkdownDoc extends XmlDoc {
		String	name;

		public MarkdownDoc(File file) throws IOException {
			super(file);
			name = file.getName().substring(0, file.getName().length() - 3) + ".html";
		}

		Document getXmlContent() throws Exception {
			StringBuilder sb = new StringBuilder(Processor.process(source));
			Matcher m = MARKER.matcher(sb);
			boolean open = false;
			int start = 0;
			while (m.find(start)) {
				String name = m.group(1);
				String repl = open ? "</div>\n" : "";
				repl += "<div id='" + name.toLowerCase() + "'>\n";
				if (m.group(2) == null)
					repl += "<p>\n";

				sb.replace(m.start(), m.end(), repl);
				open = true;
				start = m.start() + repl.length();
			}
			if (!open)
				sb.insert(0, "<div id='main'>\n");
			sb.append("</div>");

			sb.insert(0, "<content>");
			sb.append("</content>");
			Document d = db.parse(new InputSource(new StringReader(sb.toString())));

			return d;
		}

		String getName() {
			return name;
		}
	}

	class Level {
		final File				source;
		final String			path;
		final List<Doc>			documents		= new ArrayList<Doc>();
		final Map<String,Level>	subLevels		= new LinkedHashMap<String,Level>();
		long					lastModified	= 0;

		Level(File source, String path) throws IOException {
			this.source = source;
			this.path = path == null ? "/" : path;
			for (File sub : source.listFiles()) {
				if (sub.getName().startsWith("_")) // Skip directories that
													// start with an underscore
					continue;

				if (sub.isDirectory()) {
					Level nextLevel = new Level(sub, path + sub.getName() + "/");
					subLevels.put(sub.getName(), nextLevel);
					lastModified = max(lastModified, nextLevel.lastModified);
				} else {
					String name = sub.getName();
					Doc doc;
					if (name.endsWith(".md")) {
						doc = new MarkdownDoc(sub);
					} else if (name.endsWith(".html") || name.endsWith(".xml")) {
						doc = new XmlDoc(sub);
					} else if (name.endsWith(".exec")) {
						doc = new ExecDoc(sub);
					} else
						doc = new Doc(sub);

					doc.setPath(path + doc.getName());
					documents.add(doc);
					lastModified = max(lastModified, sub.lastModified());
				}
			}
		}

		void addTo(Tag site) throws IOException {
			for (Doc doc : documents) {
				Tag tag = doc.addTo(site);
				tag.addAttribute("path", path + "/" + doc.getName());
			}
			for (Level subLevel : subLevels.values())
				subLevel.addTo(site);
		}

		void transfer() throws Exception {
			File dir = IO.getFile(Site.this.target, path);
			dir.mkdirs();
			for (Doc doc : documents) {
				File target = new File(dir, doc.getName());
				doc.transfer(target);
			}
			for (Level subLevel : subLevels.values())
				subLevel.transfer();
		}

	}

	public Site(Reporter reporter, File source, File target) throws Exception {
		this.xpath = XPathFactory.newInstance().newXPath();
		this.db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		this.source = source.getAbsoluteFile();
		this.target = target.getAbsoluteFile();
		this.reporter = reporter;
		this.receipt = new File(target, ".receipt");

		this.siteSpecFile = new File(source, "_site.json");
		if (!siteSpecFile.isFile()) {
			siteSpec = new SiteSpec();
		} else {
			siteSpec = codec.dec().from(siteSpecFile).get(SiteSpec.class);
		}
		if (!siteSpec.root.isEmpty() && !siteSpec.root.endsWith("/"))
			siteSpec.root = siteSpec.root + "/";

		this.template = IO.getFile(source, siteSpec.template);
	}

	public boolean prepare() throws IOException {
		long lastModified = receipt.isFile() ? receipt.lastModified() : 0;
		this.rootLevel = new Level(source, siteSpec.root);
		return rootLevel.lastModified > lastModified || template.lastModified() > lastModified;
	}

	public void build() throws Exception {

		Tag site = new Tag("site");
		site.addAttribute("root", siteSpec.root);
		site.addAttribute("name", siteSpec.name);
		site.addAttribute("prefix", siteSpec.prefix);
		site.addAttribute("template", siteSpec.template);
		if (siteSpec.__extra != null) {
			for (Entry<String,Object> e : siteSpec.__extra.entrySet())
				site.addAttribute(e.getKey(), e.getValue());
		}

		rootLevel.addTo(site);

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		site.print(0, pw);
		pw.flush();
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		String meta = sw.toString();
		siteDocument = db.parse(new InputSource(new StringReader(meta)));

		rootLevel.transfer();
	}
}
