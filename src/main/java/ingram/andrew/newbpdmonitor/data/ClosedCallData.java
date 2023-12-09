package ingram.andrew.newbpdmonitor.data;

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

    public static ArrayList<ClosedCallData> parseDataMap(Map<String, ArrayList<Map<Object, Object>>> dataMap) {
        ArrayList<ClosedCallData> returnArray = new ArrayList<>();
        ArrayList<Map<Object, Object>> rows = dataMap.get("rows");

        for (Map<Object, Object> callDetailsMap : rows) {

            // if data is non-breaking space, then data is invalid: continue
            if (callDetailsMap.get("agency") == "&nbsp;") continue;

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

    public String getAgency() {
        return AGENCY;
    }

    public String getService() {
        return SERVICE;
    }

    public String getStartTime() {
        return START_TIME;
    }

    public String getEndTime() {
        return END_TIME;
    }

    public long getID() {
        return ID;
    }

    public String getAddress() {
        return ADDRESS;
    }
}
