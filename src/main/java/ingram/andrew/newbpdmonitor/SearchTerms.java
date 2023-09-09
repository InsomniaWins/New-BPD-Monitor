package ingram.andrew.newbpdmonitor;

import java.util.ArrayList;

public class SearchTerms {

    private static final ArrayList<String> SEARCH_TERMS = new ArrayList<>();

    public static void addSearchTerm(String searchTerm) {
        searchTerm = searchTerm.toUpperCase();
        if (SEARCH_TERMS.contains(searchTerm)) return;
        SEARCH_TERMS.add(searchTerm);
    }

    public static boolean hasSearchTerm(String searchTerm) {
        return SEARCH_TERMS.contains(searchTerm);
    }

    public static void removeSearchTerm(String searchTerm) {
        SEARCH_TERMS.remove(searchTerm);
    }

    public static String[] getSearchTerms() {
        String[] returnArray = new String[] {};
        SEARCH_TERMS.toArray(returnArray);
        return returnArray;
    }

    public static boolean containsSearchTerm(CallData callData) {
        String nature = callData.getNature();

        for (String searchTerm : SEARCH_TERMS) {
            if (nature.contains(searchTerm)) {
                return true;
            }
        }

        return false;
    }
}
