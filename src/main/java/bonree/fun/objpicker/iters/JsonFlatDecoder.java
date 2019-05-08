package bonree.fun.objpicker.iters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;

public class JsonFlatDecoder {
	private static ObjectMapper mapper = new ObjectMapper();
	private List<String> prefixNodes;
	private static Splitter splitter = Splitter.on('.');
	
	private static final String ROOT_KEY = "root";
	
	private JsonFlatDecoder() {
		this.prefixNodes = new ArrayList<String>();
	}
	
	public static JsonFlatDecoder create() {
		return new JsonFlatDecoder();
	}
	
	public JsonFlatDecoder omitPrefix(String prefixPattern) {
		this.prefixNodes.addAll(prefixPattern == null ? new ArrayList<String>() : splitter.splitToList(prefixPattern));
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public String decode(List<KeyValue> keyValues) throws JsonFlatDecoderException {
		Map<String, Object> rootObj = new HashMap<String, Object>();
		Object currentObj = null;
		
		for(KeyValue kv : keyValues) {
			String[] keyNodes = JsonKeyUtils.tripePrefix(prefixNodes, kv.keyPath());
			if(keyNodes.length == 0) {
				throw new JsonFlatDecoderException("unexpected key :" + kv.keyPath());
			}
			
			for(int i = 0; i < keyNodes.length; i++) {
				String nextNode = i < keyNodes.length - 1 ? keyNodes[i + 1] : null;
				int index = JsonKeyUtils.getIndex(keyNodes[i]);
				
				if(currentObj == null) {
					if(rootObj.isEmpty()) {
						if(index < 0) {
							currentObj = new HashMap<String, Object>();
						} else {
							currentObj = new ArrayList<Object>();
						}
						
						rootObj.put(ROOT_KEY, currentObj);
					} else {
						currentObj = rootObj.get(ROOT_KEY);
					}
				}
				
				if(index < 0) {
					Map<String, Object> map = (Map<String, Object>) currentObj;
					if(nextNode == null) {
						map.put(keyNodes[i], kv.value());
					} else {
						Object nextObj = map.get(keyNodes[i]);
						if(nextObj == null) {
							nextObj = JsonKeyUtils.getIndex(nextNode) < 0 ?
									new HashMap<String, Object>() : new ArrayList<Object>();
							
							map.put(keyNodes[i], nextObj);
						}
						
						currentObj = nextObj;
					}
				} else {
					List<Object> list = (List<Object>) currentObj;
					if(nextNode == null) {
						list.add(index, kv.value());
					} else {
						Object nextObj = null;
						if(index < list.size()) {
							nextObj = list.get(index);
						} else if(index == list.size()) {
							nextObj = JsonKeyUtils.getIndex(nextNode) < 0 ?
									new HashMap<String, Object>() : new ArrayList<Object>();
							
							list.add(nextObj);
						} else {
							throw new JsonFlatDecoderException("unexpected array index[" + index + "]:" + kv.keyPath());
						}
						
						currentObj = nextObj;
					}
				}
			}
			
			currentObj = null;
		}
		
		try {
			return mapper.writeValueAsString(rootObj.get(ROOT_KEY));
		} catch (JsonProcessingException e) {
			throw new JsonFlatDecoderException("mapping object to json error", e);
		}
	}
	
}
