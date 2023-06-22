package napster.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteServerInterface extends Remote {
    boolean join(String ipAddress) throws RemoteException;
}

