package aQute.impl.metadata.osgi;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.*;
import java.util.zip.*;

import aQute.lib.data.*;
import aQute.lib.osgi.*;
import aQute.lib.osgi.Descriptors.PackageRef;
import aQute.libg.header.*;
import aQute.libg.reporter.*;
import aQute.service.library.*;
import aQute.service.library.Library.PackageDef;
import aQute.service.library.Library.PackageType;
import aQute.service.library.Library.Revision;
import aQute.service.library.Library.Version;
import aQute.service.osgimetadata.*;

public class OSGiMetadataParser extends ReporterAdapter {
	Revision	revision;
	File		file;

	interface OSGiMetadataParserMessages extends Messages {
		WARNING NotAnOSGiBundle_(Object url);

		ERROR InvalidHeader_Clause_ForConversionTo_(String header, Attrs value, Type type);
	}

	OSGiMetadataParserMessages	msg	= getMessages(OSGiMetadataParserMessages.class);

	public OSGiMetadataParser(File file, Revision partialRevision) {
		this.file = file;
		this.revision = partialRevision;
	}

	@Override
	public void run() {
		try {
			parse();
		}
		catch (Exception e) {
			e.printStackTrace();
			exception(e, "Failed to parse %s", revision.url);
		}
	}

	void parse() throws Exception {
		Jar jar;
		try {
			jar = new Jar(file);
		}
		catch (ZipException e) {
			System.out.println("cant open this " + file);
			try {
				jar = new Jar(file.getName(), new FileInputStream(file));
			}
			catch (Exception ee) {
				System.out.println("cant open stream " + file);
				jar = new Jar(file.getName(), revision.url.toURL().openStream());
			}
		}
		Analyzer analyzer = new Analyzer();
		try {
			analyzer.setJar(jar);
			analyzer.analyze();

			revision.packages = new ArrayList<Library.PackageDef>();

			for (Entry<PackageRef,Attrs> e : analyzer.getImports().entrySet()) {
				PackageDef pd = new PackageDef();
				pd.type = PackageType.IMPORT;
				pd.name = e.getKey().getFQN();
				pd.version = e.getValue().getVersion();
				revision.packages.add(pd);
			}

			for (Entry<PackageRef,Attrs> e : analyzer.getContained().entrySet()) {
				PackageDef pd = new PackageDef();
				pd.name = e.getKey().getFQN();

				Attrs attrs = analyzer.getExports().get(e.getKey());
				if (attrs == null)
					pd.type = PackageType.PRIVATE;
				else {
					pd.type = PackageType.EXPORT;
					pd.version = e.getValue().getVersion();
				}
				revision.packages.add(pd);
			}

			Manifest manifest = analyzer.getJar().getManifest();
			if (manifest == null) {
				msg.NotAnOSGiBundle_(revision.url);
				return;
			}

			String bsn = manifest.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME);
			if (bsn == null) {
				msg.NotAnOSGiBundle_(revision.url);
				return;
			}

			// Now assuming it is an OSGi bundle
			Verifier v = new Verifier(analyzer);
			v.verify();
			getInfo(v);

			if (!isOk()) // No use importing when we have errors
				return;

			parseManifest(jar);

			revision.metadata.put("osgi", new OSGi());
		}
		finally {
			analyzer.close();
			jar.close();
		}

	}

	public void parseManifest(Jar jar) throws Exception {
		Attributes attributes = jar.getManifest().getMainAttributes();
		Domain domain = Domain.domain(attributes);

		Entry<String,Attrs> bundleSymbolicName = domain.getBundleSymbolicName();
		revision.bsn = bundleSymbolicName.getKey();

		revision.version = createVersion(domain.getBundleVersion()); // normalize

		revision.description = domain.get(Constants.BUNDLE_DESCRIPTION);
		revision.vendor = domain.get(Constants.BUNDLE_VENDOR);
		revision.docUrl = getURI(jar, domain.get(Constants.BUNDLE_DOCURL));
		revision.icon = getURI(jar, domain.get(Constants.BUNDLE_ICON));
		revision.summary = domain.get("Bundle-Release");
		h2d(revision, "scm", "url", domain.get("Bundle-SCM"), "Bundle-SCM");
		h2d(revision, "licenses", "name", domain.get("Bundle-License"), "Bundle-License");
		h2d(revision, "developers", "id", domain.get("Bundle-Developer"), "Bundle-Developer");
		h2d(revision, "contributors", "id", domain.get("Bundle-Contributor"), "Bundle-Contributor");
	}

	/**
	 * Normalize a version
	 * 
	 * @param vrs
	 * @return
	 */
	private Version createVersion(String vrs) {
		if (vrs == null)
			vrs = "0";
		aQute.libg.version.Version v = aQute.libg.version.Version.parseVersion(vrs);
		Version result = new Version();
		result.base = v.getWithoutQualifier().toString();
		result.qualifier = v.getQualifier();
		return result;
	}

	private URI getURI(Jar jar, String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> void h2d(T target, String field, String id, String spec, String header) {
		if (spec == null)
			return;

		Parameters parameters = OSGiHeader.parseHeader(spec);
		List<Map<String,String>> instances = new ArrayList<Map<String,String>>(parameters.size());

		data< ? > dtarget = data.wrap(target);
		for (Entry<String,Attrs> e : parameters.entrySet()) {
			try {

				e.getValue().put(id, e.getKey());
				instances.add(e.getValue());
				dtarget.put(field, instances);
			}
			catch (Exception ee) {
				// Conversion has likely failed
				msg.InvalidHeader_Clause_ForConversionTo_(header, e.getValue(), dtarget.getType(field));

			}
		}
	}

}
