package napster.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class RemoteServer implements RemoteServerInterface {
	private List<String> clientIPs;

    public RemoteServer() {
        clientIPs = new ArrayList<>();
    }

    public boolean join(String ipAddress) throws RemoteException {
        System.out.println("Nova requisição de cliente: " + ipAddress);
        if (!clientIPs.contains(ipAddress)) {
            System.out.println("Cliente conectado: " + ipAddress);
            clientIPs.add(ipAddress);
            return true;
        } else {
            System.out.println("Cliente já conectado: " + ipAddress);
            return false;
        }
    }

    public List<String> search() throws RemoteException {
        return clientIPs;
    }

    public static void main(String[] args) {
        try {
            RemoteServer server = new RemoteServer();
            RemoteServerInterface stub = (RemoteServerInterface) UnicastRemoteObject.exportObject(server, 0);

            // Crie o registro RMI na porta 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // Vincule o objeto remoto ao registro
            registry.bind("RemoteServer", stub);

            System.out.println("Servidor pronto para receber conexões de clientes.");
        } catch (Exception e) {
            System.err.println("Erro no servidor: " + e.toString());
            e.printStackTrace();
        }
    }
}

