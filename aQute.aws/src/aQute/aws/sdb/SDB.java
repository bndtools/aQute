package aQute.aws.sdb;

import java.util.*;

public interface SDB {
	void createDomain(String domain) throws Exception;
	void deleteAttributes(String domain, Map<String, String> ... attributes) throws Exception;
	void deleteDomain(String domain) throws Exception;
	Collection<String> listDomains() throws Exception;
	void putAttributes(String domain, Map<String, String> ... attributes) throws Exception;
	void putAttributesConditional(String domain, Map<String, String> tuple, String expectedKey, String expectedValue) throws Exception;
	Collection<Map<String,String>> select(String select) throws Exception;	
}
