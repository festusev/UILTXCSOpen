package Outlet.uil;

import com.google.gson.*;

import java.util.ArrayList;

public class Clarification {
    static int nextGlobalIndex = 0;

    int index;
    short uid;
    String question;
    String response;
    boolean responded;

    public Clarification(short uid, String question, String response, boolean responded) {
        index = nextGlobalIndex;
        nextGlobalIndex++;

        this.uid = uid;
        this.question = question;
        this.response = response;
        this.responded = responded;
    }

    public Clarification(int index, short uid, String question, String response, boolean responded) {
        this.index = index;
        this.uid = uid;
        this.question = question;
        this.response = response;
        this.responded = responded;

        if(index >= nextGlobalIndex) {
            nextGlobalIndex = index + 1;
        }
    }

    public JsonArray toJson() {
        JsonArray array = new JsonArray();
        array.add(index);
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
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        return new Clarification(jsonArray.get(0).getAsInt(), jsonArray.get(1).getAsShort(), jsonArray.get(2).getAsString(),
                jsonArray.get(3).getAsString(), jsonArray.get(4).getAsBoolean());

        // return gson.fromJson(json, Clarification.class);
        // Type collectionType = new TypeToken<Collection<Clarification>>(){}.getType();
        // Collection<Clarification> myCustomClasses = gson.fromJson(json, collectionType);
    }
    public static ArrayList<Clarification> fromJsonToArray(String json) {
        System.out.println("from json to array='"+json+"'");
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();

        ArrayList<Clarification> list = new ArrayList<>();
        for(JsonElement element: jsonArray) {
            JsonArray clarification = element.getAsJsonArray();
            list.add(new Clarification(clarification.get(0).getAsInt(), clarification.get(1).getAsShort(), clarification.get(2).getAsString(),
                    clarification.get(3).getAsString(), clarification.get(4).getAsBoolean()));
        }
        return list;
    }
}