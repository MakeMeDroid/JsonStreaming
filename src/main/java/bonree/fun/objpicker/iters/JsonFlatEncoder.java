package bonree.fun.objpicker.iters;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonFlatEncoder {
	private static final JsonFactory factory = new JsonFactory();
	private JsonParser parser;
	private Throwable cause;
	
	private LinkedList<String> stack = new LinkedList<String>();
	
	private List<Handler> handlers = new ArrayList<Handler>();
	
	private JsonFlatEncoder(JsonParser parser, Throwable t) {
		this.parser = parser;
		this.cause = t;
	}
	
	public static JsonFlatEncoder stream(File f) {
		JsonParser parser = null;
		Throwable cause = null;
		try {
			parser = factory.createParser(f);
		} catch (Exception e) {
			cause = e;
		}
		
		return new JsonFlatEncoder(parser, cause);
	}
	
	public static JsonFlatEncoder stream(InputStream in) {
		JsonParser parser = null;
		Throwable cause = null;
		try {
			parser = factory.createParser(in);
		} catch (Exception e) {
			cause = e;
		}
		
		return new JsonFlatEncoder(parser, cause);
	}
	
	public static JsonFlatEncoder stream(String content) {
		JsonParser parser = null;
		Throwable cause = null;
		try {
			parser = factory.createParser(content);
		} catch (Exception e) {
			cause = e;
		}
		
		return new JsonFlatEncoder(parser, cause);
	}
	
	public static interface Handler {
		/**
		 * handle key value from json flattening stream
		 * 
		 * @param kv
		 * @return true if handling completed; else deliver kv to next handler
		 */
		boolean handle(KeyValue kv);
		
		/**
		 * json key value is end
		 */
		void end();
		
		/**
		 * json flattening failed
		 * 
		 * @param t
		 */
		void fail(Throwable t);
	}
	
	public JsonFlatEncoder handleWith(Handler handler) {
		handlers.add(handler);
		return this;
	}
	
	private void notifyFail(Throwable t) {
		if(handlers.isEmpty()) {
			return;
		}
		
		try {
			handlers.get(0).fail(t);
		} catch (Exception e) {
			//ignore
		}
	}
	
	private void notifyKeyValue(KeyValue kv) {
		if(handlers.isEmpty()) {
			return;
		}
		
		for(Handler handler : handlers) {
			try {
				if(handler.handle(kv)) {
					break;
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}
	
	private void notifyEndEvent() {
		if(handlers.isEmpty()) {
			return;
		}
		
		for(Handler handler : handlers) {
			try {
				handler.end();
			} catch (Exception e) {
				// ignore
			}
		}
	}
	
	public void start() {
		start(null);
	}
	
	public void start(Executor executor) {
		if(parser == null) {
			notifyFail(new IllegalStateException("json parser is null", cause));
			return;
		}
		
		if(executor == null) {
			executor = new Executor() {
				
				@Override
				public void execute(Runnable command) {
					command.run();
				}
			};
		}
		
		executor.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					for(;;) {
						JsonToken token = parser.nextToken();
						
						if(token == JsonToken.START_ARRAY) {
							stack.addLast(JsonKeyUtils.index(0));
						} else if(token == JsonToken.START_OBJECT) {
							continue;
						} else if(token == JsonToken.FIELD_NAME) {
							stack.addLast(parser.getText());
						} else if(token == JsonToken.VALUE_STRING) {
							notifyKeyValue(new KeyValue(copyKeyPath(stack), parser.getText()));
							checkAndRemove();
						} else if(token == JsonToken.VALUE_NUMBER_INT) {
							notifyKeyValue(new KeyValue(copyKeyPath(stack), parser.getIntValue()));
							checkAndRemove();
						} else if(token == JsonToken.VALUE_NUMBER_FLOAT) {
							notifyKeyValue(new KeyValue(copyKeyPath(stack), parser.getFloatValue()));
							checkAndRemove();
						} else if(token == JsonToken.END_OBJECT) {
							checkAndRemove();
							
							if(stack.isEmpty()) {
								notifyEndEvent();
								break;
							}
						} else if(token == JsonToken.END_ARRAY) {
							stack.removeLast();
							if(stack.isEmpty()) {
								notifyEndEvent();
								break;
							}
						}
					}
				} catch (Exception e) {
					notifyFail(e);
				}
			}
		});
	}
	
	private List<String> copyKeyPath(List<String> l) {
		List<String> keyPath = new ArrayList<String>();
		keyPath.addAll(l);
		
		return keyPath;
	}
	
	private void checkAndRemove() {
		String item = stack.removeLast();
		int index = JsonKeyUtils.getIndex(item);
		if(index >= 0) {
			stack.addLast(JsonKeyUtils.index(++index));
		}
	}
}
