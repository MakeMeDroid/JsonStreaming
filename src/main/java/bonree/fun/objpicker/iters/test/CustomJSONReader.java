package bonree.fun.objpicker.iters.test;


import java.io.Reader;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.parser.JSONToken;

public class CustomJSONReader extends JSONReader {

	public CustomJSONReader(Reader reader) {
		super(reader);
	}

	public boolean hasNextObject() {
		return peek() != JSONToken.RBRACE;
	}
	
	public boolean hasNextArray() {
		return peek() != JSONToken.RBRACKET;
	}
}
