package bonree.fun.objpicker.iters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import bonree.fun.objpicker.iters.JsonFlatEncoder.Handler;

public class Test {
	
	public static void main(String[] args) throws Exception {
		String pp = "__.result.events.__";
		List<String> pattern = Splitter.on('.').splitToList(pp);
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < 1; i++) {
			run(pp, pattern);
		}
		System.out.println((System.currentTimeMillis() - start));
	}
	
	private static void run(String pp, List<String> pattern) {
		JsonFlatEncoder.stream(new File("/root/temp/select.result")).handleWith(new Handler() {
			private String prefix = "";
			private List<KeyValue> kvs = new ArrayList<KeyValue>();
			private int count = 0;
			private Joiner join = Joiner.on('.');
			
			@Override
			public boolean handle(KeyValue kv) {
				String[] curprefix = JsonKeyUtils.extractPrefix(pattern, kv.keyPath());
				if(curprefix.length > 0) {
					String np = join.join(curprefix);
					if(np.equals(prefix)) {
						kvs.add(kv);
						return true;
					} else {
						decode();
						
						prefix = np;
						kvs.add(kv);
						return true;
					}
				}
				
				decode();
				
				return true;
			}
			
			@Override
			public void fail(Throwable t) {
				t.printStackTrace();
			}

			@Override
			public void end() {
				decode();
				
				System.out.println("total:" + count);
			}
			
			private void decode() {
				if(!kvs.isEmpty()) {
					try {
						String s = JsonFlatDecoder.create().omitPrefix(pp).decode(kvs);
//						System.out.println(s);
						count++;
					} catch (JsonFlatDecoderException e) {
						e.printStackTrace();
					}
					
					kvs.clear();
				}
			}
		}).start();
	}
}
