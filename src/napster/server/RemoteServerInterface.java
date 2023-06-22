package napster.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteServerInterface extends Remote {
    boolean join(String ipAddress, int port, List<String> fileNames) throws RemoteException;
    List<ClientInfo> search(String fileName) throws RemoteException;
}
