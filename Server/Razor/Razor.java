package HTTPServer.Server.Razor;

import java.util.ArrayList;

public class Razor<T> {

    private final ArrayList<String> keys = new ArrayList<>();
    private final ArrayList<String> patterns = new ArrayList<>();
    private final ArrayList<TriFunction<T>> functions = new ArrayList<>();
    private final ArrayList<T>  ts = new ArrayList<>();
    public void put(String key, String pattern, TriFunction<T> f,  T t) {
        keys.add(key);
        patterns.add(pattern.replace("$", "[^}]+"));
        functions.add(f);
        ts.add(t);
    }

    public void remove(String key) {
        int index = keys.indexOf(key);
        patterns.remove(index);
        functions.remove(index);
        ts.remove(index);
        keys.remove(index);
    }

    public int size() {
        return Math.min(Math.min(this.keys.size(), this.patterns.size()), this.functions.size());
    }

    public String forEach (String response) {
        for(int i = 0; i < this.size(); i++) {
            response = functions.get(i).apply(response, patterns.get(i), ts.get(i));
        }
        return response;
    }


    public interface TriFunction<T> {
        String apply(String response, String pattern, T t);
    }

}
