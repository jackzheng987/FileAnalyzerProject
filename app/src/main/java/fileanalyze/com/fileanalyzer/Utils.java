package fileanalyze.com.fileanalyzer;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();


    public static List<File> getFileFromPath(String dir) {

        Collection<File> files = FileUtils.listFiles(new File(dir), null, true);

        return new ArrayList<>(files);
    }

    public static String getExtension(File file) {
        try {
            String result = file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase();
            Logger.d(TAG, "getExtension" + result);
            return result;
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
        return null;
    }

    public static <K, V extends Comparable<? super V>> HashMap<K, V>
    sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        HashMap<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }


}
