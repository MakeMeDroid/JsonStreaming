package bonree.fun.objpicker.iters;

import java.util.List;

public final class JsonKeyUtils {
	
	public static String index(int i) {
		return "_" + i;
	}
	
	public static int getIndex(String s) {
		if(!s.startsWith("_")) {
			return -1;
		}
		try {
			return Integer.parseInt(s.substring(1));
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static boolean isArrayIndexPattern(String s) {
		return s != null && s.equals("__");
	}
	
	public static String[] extractPrefix(List<String> prefixPattern, List<String> keyPath) {
		if(keyPath.size() < prefixPattern.size()) {
			return new String[0];
		}
		
		String[] nodes = new String[prefixPattern.size()];
		for(int i = 0; i < prefixPattern.size(); i++) {
			String prefixNode = prefixPattern.get(i);
			String keyNode = keyPath.get(i);
			if(JsonKeyUtils.isArrayIndexPattern(prefixNode)) {
				if(JsonKeyUtils.getIndex(keyNode) >= 0) {
					nodes[i] = keyNode;
					continue;
				} else {
					return new String[0];
				}
			}
			
			if(!prefixNode.equals(keyNode)) {
				return new String[0];
			}
			
			nodes[i] = keyNode;
		}
		
		return nodes;
	}
	
	public static String[] tripePrefix(List<String> prefixNodes, List<String> keyNodes) {
		int nodeLength = keyNodes.size() - prefixNodes.size();
		if(nodeLength <= 0) {
			return new String[0];
		}
		
		String[] nodes = new String[nodeLength];
		int i = 0;
		for(; i < prefixNodes.size(); i++) {
			String prefixNode = prefixNodes.get(i);
			String keyNode = keyNodes.get(i);
			if(JsonKeyUtils.isArrayIndexPattern(prefixNode)) {
				if(JsonKeyUtils.getIndex(keyNode) >= 0) {
					continue;
				} else {
					return new String[0];
				}
			}
			
			if(!prefixNode.equals(keyNode)) {
				return new String[0];
			}
		}
		
		for(int j = 0; j < nodeLength; j++) {
			nodes[j] = keyNodes.get(j + i);
		}
		
		return nodes;
	}
}
