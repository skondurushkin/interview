package org.infotex.skondurushkin;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class Helpers {

	private static ObjectMapper om;
	static {
		om = new ObjectMapper();
	}
	
	public static ObjectMapper getObjectMapper() {
		return om;
	}

	public static ObjectWriter getObjectWriter() {
		return getObjectMapper().writer().withDefaultPrettyPrinter();
	}

	/**
	 * Use this method in implementations of toString()
	 * 
	 * @param o The object to stringize
	 * @return string representation of object in json format
	 */
	public static String toJsonString(Object o) {
		try {
			return getObjectWriter().writeValueAsString(o);
		} catch (JsonProcessingException e) {
			// String msg = e.getMessage();
		}
		return null;
	}	
	
	public static JsonNode  asJson(Object o) {
		return getObjectMapper().valueToTree(o);
	}
	public static ArrayNode asJsonArray(List<?> list) {
		ArrayNode ret = JsonNodeFactory.instance.arrayNode(list.size());
		list.forEach(o -> ret.add(asJson(o)));
		return ret;
	}
	
	
	public static void rethrowAsRunimeException(Throwable t) {
		if (t == null)
			return;
		if (t instanceof RuntimeException)
			throw (RuntimeException)t;
		throw new RuntimeException(t);
	}
}
