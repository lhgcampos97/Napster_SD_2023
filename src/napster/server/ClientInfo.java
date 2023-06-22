package napster.server;

import java.util.List;

public class ClientInfo {
    private String ip;
    private int port;
    private List<String> fileNames;

    public ClientInfo(String ip, int port, List<String> fileNames) {
        this.ip = ip;
        this.port = port;
        this.fileNames = fileNames;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public List<String> getFileNames() {
        return fileNames;
    }
}

