package HTTPServer.Server;

import HTTPServer.Server.Handler.RequestHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

public class Server {

    private RequestHandler handler = new RequestHandler(this, null);

    private int port;
    private String hostName;
    private HttpServer server;

    public Server (String addr, int port) throws IOException {
        this.port = port;
        this.hostName = addr;
        this.server = HttpServer.create();
        this.server.bind(new InetSocketAddress(this.hostName,this.port), 0);
        this.server.createContext("/", handler);
        this.server.start();
        System.out.println(String.format("Server started on Port %s and is reachable under %s", this.port, String.format("http://%s:%s", this.hostName, this.port)));
    }

    public void open(String path) throws IOException {
        if(Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) Desktop.getDesktop().browse(URI.create(String.format("http://%s:%s/%s", this.hostName, this.port,path)));
    }

    public void open() throws IOException {
        this.open("");
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return hostName;
    }

    public RequestHandler getHandler () {
        return this.handler;
    }
}
