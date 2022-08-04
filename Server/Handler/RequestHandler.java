package HTTPServer.Server.Handler;

import HTTPServer.Server.Razor.Razor;
import HTTPServer.Server.Server;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestHandler implements HttpHandler {

    private Server server;
    private HashMap<String, String> responses = new HashMap<>();
    private String root = "Site", indexSite = "index";

    public final Razor<Class<?>> RAZORS = new Razor<>();

    public RequestHandler(Server server, Class<?> StringFile) {
        this.server = server;
        this.parse();

        // Add basic Razors
        RAZORS.put("String", "@{String:$}", (str, pattern, c) -> {
            final String[] strr = {str};
            Arrays.stream(c.getDeclaredFields()).forEach((field) -> {
                try {
                    strr[0] = strr[0].replace(pattern.replace("[^}]+", field.getName()), field.get(new String()).toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            return strr[0];
        }, StringFile);

        RAZORS.put("Link", "@{Link:$}", (str, pattern, c) -> {
            final String[] strr = {str};
            responses.forEach((key, resp) -> strr[0] = strr[0].replace(pattern.replace("[^}]+", key), String.format("<a href=\"%s\">%s</a>",key.replace(".jhtml", "").replace(indexSite,"/"),key.replace(".jhtml", "").replace(indexSite,"Home"))));
            return strr[0];
        }, null);

        RAZORS.put("Include", "@{Include:$}", (str, pattern, c) ->{
            final String[] strr = {str};
            responses.forEach((key, resp) -> strr[0] = strr[0].replace(pattern.replace("[^}]+", key), String.format("<%s>%s</%s>",key.endsWith("js")?"script":"style",resp,key.endsWith("js")?"script":"style")));
            return strr[0];
        }, null);

        RAZORS.put("Title", "@{Title:&}", (str, pattern, c) ->{
            int[] arr = getContent(pattern, str);
            String title = str.substring(arr[0], arr[1]);
            str = str.replace("<head>", String.format("<head>\n<title>%s</title>",title));
            str = str.replace(String.format("@{Title:%s}",title),"");
            return str;
        }, null);
    }

    public static int[] getContent(String pattern, String str) {
        String[] a = pattern.split("&");
        if(a.length == 0)
            return new int[]{0,0};

        return new int[]{str.indexOf(a[0])+a[0].length(),str.indexOf(a[1])};
    }

    /**
     * Parse all data found in this directory
     */
    private void parse() {
        Arrays.stream(Objects.requireNonNull(new File(this.root).listFiles())).forEach(this::read);
    }

    /**
     * Read the file but if the file is a directory then repeat
     */
    private void read(File f) {
        if (f.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(f.listFiles())).forEach(this::read);
        } else {
            try {
                Scanner sc = new Scanner(new FileReader(f));
                StringBuffer sb = new StringBuffer();
                while (sc.hasNextLine()) {
                    sb.append(String.format("%s\n",sc.nextLine()));
                }
                this.responses.put(f.getName(), sb.toString());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void handle(HttpExchange exchange) {
        // get the requested file / path
        String requested = exchange.getRequestURI().getPath().replaceAll("/","");

        URI uri = URI.create(String.format("ws://%s:%s/%s",server.getAddress(),server.getPort(),requested));


        if(exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            // GET
            // TODO GET Method interpretation
        } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            // POST
            StringBuffer postMethodContent = new StringBuffer();
            Scanner rb = new Scanner(exchange.getRequestBody());
            while (rb.hasNextLine()) {
                postMethodContent.append(rb.nextLine());
            }
            System.out.println("POST: " + postMethodContent);
        }


        // if its index the replace with index
        if (requested.equals("")) {
            requested = this.indexSite;
        }
        // Get response
        String response = getResponse(requested);
        // if there is no response get the error site
        if (response == null)
            this.responses.get("Error:404");
        // if there isnt even an error site return a blank error message
        if (response == null)
            response = "<h1>Error 404</h1><br><p>Make sure to set a error page!</p>";

        try {
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Set the new Root path like "site" not "/" in front
     * @param root the new root path like "site"
     */
    public void setRoot(String root) {
        this.root = root;
        this.responses = new HashMap<>();
        this.parse();
    }

    /**
     * @return the root
     */
    public String getRoot() {
        return this.root;
    }

    /**
     * Get the requested file
     *
     * and apply every possible razor on it
     *
     * @param fileName
     * @return
     */
    public String getResponse(String fileName) {
        fileName = String.format("%s.jhtml",fileName);
        final String[] response = {this.responses.get(fileName)};

        response[0] = RAZORS.forEach(response[0]);

        return response[0];
    }

    /**
     * Set the Error sites
     *
     * make sure to set a 404 site
     */
    public void setError(String code , String htmlCode) {
        this.responses.replace(String.format("Error:%s", code), htmlCode);
        this.responses.put(String.format("Error:%s", code), htmlCode);
    }

    public HashMap<String, String> getResponses() {
        return responses;
    }

    public void changeStringClass(Class<?> ca) {
        RAZORS.remove("String");
        RAZORS.put("String", "@{String:$}", (str, pattern, c) -> {
            final String[] strr = {str};
            Arrays.stream(c.getDeclaredFields()).forEach((field) -> {
                try {
                    strr[0] = strr[0].replace(pattern.replace("[^}]+", field.getName()), field.get(new String()).toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            return strr[0];
        }, ca);
    }

    public void setIndexSite (String indexSite) {
        this.indexSite = indexSite;
    }
}
