package napster.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class RemoteServer implements RemoteServerInterface {
	private List<ClientInfo> clientIPs;

    public RemoteServer() {
        clientIPs = new ArrayList<>();
    }

    public boolean join(String ipAddress, int port, List<String> fileNames) throws RemoteException {
        String clientAddress = ipAddress + ":" + port;

        for (ClientInfo clientInfo : clientIPs) {
            if (clientInfo.getIp().equals(ipAddress) && clientInfo.getPort() == port) {
                System.out.println("Cliente já conectado: " + clientAddress);
                return false;
            }
        }

        ClientInfo newClient = new ClientInfo(ipAddress, port, fileNames);
        clientIPs.add(newClient);

        System.out.println("Cliente conectado: " + clientAddress);
        return true;
    }

    public List<ClientInfo> search(String fileName) throws RemoteException {
        List<ClientInfo> clientsWithFile = new ArrayList<>();

        for (ClientInfo client : clientIPs) {
            if (client.getFileNames().contains(fileName)) {
                clientsWithFile.add(client);
            }
        }

        return clientsWithFile;
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

