package ingram.andrew.newbpdmonitor;

public class OpenCallData {
    final String AGENCY;
    final String SERVICE;
    final String START_TIME;
    final long ID;
    final String NATURE;
    final String ADDRESS;

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
}
