package bonree.fun.objpicker.iters.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class JSONTest1 {
    static boolean switchFlag = true;
    static int count = 0;
    static CustomJSONReader reader;
    static {
        try {
            reader = new CustomJSONReader(new FileReader(new File("/root/temp/select.result")));
            reader.startArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void testIt() {
        
        while(JSONTest1.hasNext()) {
            JSONTest1.next();
//            System.out.println();
            count++;
        }
        
        close();
    }
    public static void main(String[] args) throws Exception {
        long begin = System.currentTimeMillis();
        testIt();
        long end = System.currentTimeMillis();
        System.out.println("use:" + (end - begin) + " ms");
        System.out.println(count);
    }
    
    static boolean hasFirst() {
        boolean outter = reader.hasNext();
        boolean inner = false;
        if (!outter) {
            return false;
        }
        reader.startObject();
        String temp;
        while (!"result".equals(temp = reader.readString())) {
//            System.out.println(temp + "  cycle ...");
        }
        reader.startObject();
       
        String key;
        while (true) {
            key = reader.readString();
            if(key.equals("events")) {
                reader.startArray();
                inner = reader.hasNextArray();     
                if(inner) {
                    switchFlag = false;
                }
                break;
            }
        }
        return inner;
    }
    
    static boolean hasSequence() {
        boolean inner =reader.hasNextArray();
        if(inner) {
            return true;
        }
        reader.endArray();
        
        reader.endObject();
        reader.endObject();
        switchFlag = true;
        return hasFirst();
    }

    static boolean hasNext() {
        if(!switchFlag) {
            return hasSequence();
        }
        return hasFirst(); 
    }

    static Object next() {
        return reader.readObject();
    }
    
    static void close() {
        reader.endArray();
        reader.close();
    }

}
