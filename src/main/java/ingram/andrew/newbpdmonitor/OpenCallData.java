package ingram.andrew.newbpdmonitor;

import java.util.ArrayList;
import java.util.Map;

public class OpenCallData implements CallData{

    private final String AGENCY;
    private final String SERVICE;
    private final String START_TIME;
    private final long ID;
    private final String NATURE;
    private final String ADDRESS;

    public OpenCallData(String agency, String service, String startTime, long id, String nature, String address) {
        this.AGENCY = agency;
        this.SERVICE = service;
        this.START_TIME = startTime;
        this.ID = id;
        this.NATURE = nature;
        this.ADDRESS = address;
    }

    public String toString() {
        String returnString = "Open Call: {";

        returnString = returnString + "agency : " + AGENCY + ", ";
        returnString = returnString + "service : " + SERVICE + ", ";
        returnString = returnString + "start_time : " + START_TIME + ", ";
        returnString = returnString + "id : " + ID + ", ";
        returnString = returnString + "nature : " + NATURE + ", ";
        returnString = returnString + "address : " + ADDRESS;

        returnString = returnString + "}";
        return returnString;
    }

    public static ArrayList<OpenCallData> parseDataMap(Map<?,?> dataMap) {
        ArrayList<OpenCallData> returnArray = new ArrayList<>();
        @SuppressWarnings("unchecked") ArrayList<Map<Object, Object>> rows = (ArrayList<Map<Object, Object>>) dataMap.get("rows");

        for (Map<Object, Object> callDetailsMap : rows) {

            String agency = (String) callDetailsMap.get("agency");
            String service = (String) callDetailsMap.get("service");
            String startTime = (String) callDetailsMap.get("starttime");
            long id = Long.parseLong((String) callDetailsMap.get("id"));
            String nature = (String) callDetailsMap.get("nature");
            String address = (String) callDetailsMap.get("address");

            returnArray.add(new OpenCallData(agency, service, startTime, id, nature, address));
        }

        return returnArray;
    }

    public String getAgency() {
        return AGENCY;
    }

    public String getService() {
        return SERVICE;
    }

    public String getStartTime() {
        return START_TIME;
    }

    public long getID() {
        return ID;
    }

    public String getNature() {
        return NATURE;
    }

    public String getAddress() {
        return ADDRESS;
    }
}
