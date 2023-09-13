package ingram.andrew.newbpdmonitor;

import java.io.*;
import java.util.ArrayList;

public class SearchTerms {

    private static final ArrayList<String> SEARCH_TERMS = new ArrayList<>();

    private static final ArrayList<SearchTermsEventListener> LISTENERS = new ArrayList<>();

    public static boolean addSearchTerm(String searchTerm) {
        searchTerm = searchTerm.toUpperCase();

        if (SEARCH_TERMS.contains(searchTerm) || searchTerm.isEmpty()) return false;

        SEARCH_TERMS.add(searchTerm);

        save();

        searchTermsUpdated();

        return true;
    }

    public static boolean hasSearchTerm(String searchTerm) {
        return SEARCH_TERMS.contains(searchTerm);
    }

    public static void removeSearchTerm(String searchTerm) {
        SEARCH_TERMS.remove(searchTerm);
        searchTermsUpdated();
    }

    public static String[] getSearchTerms() {
        String[] returnArray = new String[SEARCH_TERMS.size()];
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

    private static void searchTermsUpdated() {
        for (SearchTermsEventListener listener : LISTENERS) {
            listener.onSearchTermsUpdated();
        }
    }

    public static void addListener(SearchTermsEventListener newListener) {
        if (LISTENERS.contains(newListener)) return;
        LISTENERS.add(newListener);
    }

    public static void removeListener(SearchTermsEventListener listener) {
        LISTENERS.remove(listener);
    }

    public static void load() {
        File saveFile = new File("search-terms.txt");
        try {
            FileReader fileReader = new FileReader(saveFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line = bufferedReader.readLine();
            while (line != null) {
                addSearchTerm(line);
                line = bufferedReader.readLine();
            }

            fileReader.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        File saveFile = new File("search-terms.txt");
        try {
            FileWriter writer = new FileWriter(saveFile);

            for (String searchTerm : SEARCH_TERMS) {
                writer.write(searchTerm + "\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
