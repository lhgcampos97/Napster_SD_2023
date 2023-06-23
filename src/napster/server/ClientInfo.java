package napster.server;

import java.io.Serializable;
import java.util.List;

public class ClientInfo implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4499890955037846892L;
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

