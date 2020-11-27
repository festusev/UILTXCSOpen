package Outlet.uil;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public class Clarification {
    short uid;
    String question;
    String response;
    boolean responded;
    private static Gson gson = new GsonBuilder()
                .registerTypeAdapter(Clarification.class, new ClarificationDeserializer())
                .create();
    public Clarification(short uid, String question, String response, boolean responded) {
        this.uid = uid;
        this.question = question;
        this.response = response;
        this.responded = responded;
    }

    public JsonArray toJson() {
        JsonArray array = new JsonArray();
        array.add(uid);
        array.add(question);
        array.add(response);
        array.add(responded);

        return array;
    }

    public static JsonArray toJson(ArrayList<Clarification> clarifications) {
        JsonArray array = new JsonArray();
        for(Clarification clarification: clarifications) {
            array.add(clarification.toJson());
        }
        return array;
    }

    public static Clarification fromJson(String json) {
        return gson.fromJson(json, Clarification.class);
        // Type collectionType = new TypeToken<Collection<Clarification>>(){}.getType();
        // Collection<Clarification> myCustomClasses = gson.fromJson(json, collectionType);
    }
    public static ArrayList<Clarification> fromJsonToArray(String json) {
        System.out.println("from json to array='"+json+"'");
        Type collectionType = new TypeToken<ArrayList<Clarification>>(){}.getType();

        return gson.fromJson(json, collectionType);
    }
}

class ClarificationDeserializer implements JsonDeserializer<Clarification> {
    @Override
    public Clarification deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        return new Clarification(array.get(0).getAsShort(), array.get(1).getAsString(),
                array.get(2).getAsString(), array.get(3).getAsBoolean());
    }
}