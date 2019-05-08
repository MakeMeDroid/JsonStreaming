package bonree.fun.objpicker.iters;

import java.util.List;

import com.google.common.base.Joiner;

public class KeyValue {
	private final List<String> keyPath;
	private final Object value;
	
	public KeyValue(List<String> key, Object value) {
		this.keyPath = key;
		this.value = value;
	}

	public List<String> keyPath() {
		return keyPath;
	}

	public Object value() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{")
		       .append(Joiner.on('.').join(keyPath))
		       .append(":")
		       .append(value)
		       .append("}");
		
		return builder.toString();
	}
}
