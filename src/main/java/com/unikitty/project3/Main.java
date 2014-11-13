package com.unikitty.project3;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.unikitty.jackson.Model;

/**
 * Here is your main, I wrote JacksonExample so you guys can see how to use it.
 * Eventually main will of course start our servers and do other magical things.
 */
public class Main {

    public static void main( String[] args ) {
        try {
            Model m = new Model(1, 2);
            String JSONString = ObjectToJackson(m);
            System.out.println("\nAttempting to map POJO to JSON String:");
            System.out.println(JSONString + "\n");
            Model n = JSONToModel(JSONString);
            System.out.println("Attempting to map JSON String to POJO");
            if (m.equals(n))
                System.out.println("Successfully deserialized JSON\n");
            }
        catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * Maps Model m to a JSON String with Jackson
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     */
    private static String ObjectToJackson(Model m) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper obj = new ObjectMapper();
        return obj.writeValueAsString(m);
    }
    
    /**
     * Maps a JSON String to a Model
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    private static Model JSONToModel(String mString) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper obj = new ObjectMapper();
        return (Model) obj.readValue(mString, Model.class);
    }
    
}
