package napster.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteServerInterface extends Remote {
    boolean join(String ipAddress) throws RemoteException;
    List<String> search() throws RemoteException;
}
