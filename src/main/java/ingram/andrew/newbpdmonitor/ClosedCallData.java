package ingram.andrew.newbpdmonitor;

import java.util.ArrayList;
import java.util.Map;

public class ClosedCallData implements CallData{


    private final String AGENCY;
    private final String SERVICE;
    private final String START_TIME;
    private final String END_TIME;
    private final long ID;
    private final String NATURE;
    private final String ADDRESS;

    public ClosedCallData(String agency, String service, String startTime, String endTime, long id, String nature, String address) {
        this.AGENCY = agency;
        this.SERVICE = service;
        this.START_TIME = startTime;
        this.END_TIME = endTime;
        this.ID = id;
        this.NATURE = nature;
        this.ADDRESS = address;
    }

    @Override
    public String getNature() {
        return NATURE;
    }

    public String toString() {
        String returnString = "Closed Call: {";

        returnString = returnString + "agency : " + AGENCY + ", ";
        returnString = returnString + "service : " + SERVICE + ", ";
        returnString = returnString + "start_time : " + START_TIME + ", ";
        returnString = returnString + "end_time : " + END_TIME + ", ";
        returnString = returnString + "id : " + ID + ", ";
        returnString = returnString + "nature : " + NATURE + ", ";
        returnString = returnString + "address : " + ADDRESS;

        returnString = returnString + "}";
        return returnString;
    }

    public static ArrayList<ClosedCallData> parseDataMap(Map<?,?> dataMap) {
        ArrayList<ClosedCallData> returnArray = new ArrayList<>();
        ArrayList<Map<Object, Object>> rows = (ArrayList<Map<Object, Object>>) dataMap.get("rows");

        for (Map<Object, Object> callDetailsMap : rows) {

            String agency = (String) callDetailsMap.get("agency");
            String service = (String) callDetailsMap.get("service");
            String startTime = (String) callDetailsMap.get("starttime");
            String endTime = (String) callDetailsMap.get("closetime");
            long id = Long.parseLong((String) callDetailsMap.get("id"));
            String nature = (String) callDetailsMap.get("nature");
            String address = (String) callDetailsMap.get("address");

            returnArray.add(new ClosedCallData(agency, service, startTime, endTime, id, nature, address));
        }

        return returnArray;
    }
}
