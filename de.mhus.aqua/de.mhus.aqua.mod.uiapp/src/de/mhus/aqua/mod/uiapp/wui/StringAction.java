package de.mhus.aqua.mod.uiapp.wui;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.mhus.lib.parser.CompiledString;
import de.mhus.lib.parser.StringCompiler;

public class StringAction implements Action {

	private CompiledString compiled;
	private Map<String, Object> attr;
	
	
	public StringAction(String pattern, final String uid) {
		attr = new Map<String, Object>() {

			@Override
			public void clear() {}
			@Override
			public boolean containsKey(Object key) {return true;}
			@Override
			public boolean containsValue(Object value) {return true;}
			@Override
			public Set<Entry<String, Object>> entrySet() {return null;}
			@Override
			public Object get(Object key) {return uid;}
			@Override
			public boolean isEmpty() {return false;}
			@Override
			public Set<String> keySet() {return null;}
			@Override
			public Object put(String key, Object value) {return null;}
			@Override
			public void putAll(Map<? extends String, ? extends Object> m) {}
			@Override
			public Object remove(Object key) {return null;}
			@Override
			public int size() {return 1;}
			@Override
			public Collection<Object> values() {return null;}
		};
		compiled = StringCompiler.compile(pattern);
	}
	
	public StringAction(String pattern, Map<String,Object> attr) {
		compiled = StringCompiler.compile(pattern);
		this.attr = attr;
	}
	@Override
	public String paint() {
		return compiled.execute(attr);
	}

}
