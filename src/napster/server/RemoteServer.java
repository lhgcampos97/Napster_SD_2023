package napster.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/* 
 * Modelo de servidor em uma rede P2P similar ao sistema Napster, permite a conexão de N clientes
 * com um servidor, o qual deve ser executado antes dos clientes. Utiliza conexões RMI para 
 * comunicação com o servidor e conexão TCP para a transferência de arquivos.
 * 
 * Projeto realizado para a disciplina Sistemas Distribuídos - UFABC
 * 
 * @author Lucas Henrique Gois de Campos
 * 
 */

public class RemoteServer implements RemoteServerInterface {
	private List<ClientInfo> clients; // Lista de clientes conectados ao server
	
    public RemoteServer() {
        clients = new ArrayList<>();
    }

    /**
     * Método que permite que um cliente se conecte ao servidor.
     * O cliente fornece seu endereço IP, porta e lista de nomes de arquivos.
     * Verifica se o cliente já está conectado e adiciona o novo cliente à lista se for uma nova conexão.
     *
     * @param ipAddress Endereço IP do cliente
     * @param port      Porta do cliente
     * @param fileNames Lista de nomes de arquivos do cliente
     * @return true se o cliente foi conectado com sucesso, false se o cliente já estava conectado
     * @throws RemoteException em caso de erro de comunicação remota
     */
    public boolean join(String ipAddress, int port, List<String> fileNames) throws RemoteException {
        String clientAddress = ipAddress + ":" + port;

        for (ClientInfo clientInfo : clients) {
            if (clientInfo.getIp().equals(ipAddress) && clientInfo.getPort() == port) {
                System.out.println("Cliente já conectado: " + clientAddress);
                return false;
            }
        }

        ClientInfo newClient = new ClientInfo(ipAddress, port, fileNames);
        clients.add(newClient);

        System.out.println("Peer: " + clientAddress+" adicionado com arquivos "+Arrays.toString(fileNames.toArray()));
        return true;
    }
    
    /**
     * Método para realizar a busca de um arquivo no servidor.
     *
     * @param ipAddress Endereço IP do cliente
     * @param port Porta do cliente
     * @param fileName Nome do arquivo a ser buscado
     * @return Lista de ClientInfo dos clientes que possuem o arquivo
     * @throws RemoteException em caso de erro de comunicação remota
     */
    public List<ClientInfo> search(String ip, int port, String fileName) throws RemoteException {
        List<ClientInfo> clientsWithFile = new ArrayList<>();

        for (ClientInfo client : clients) {
            if (client.getFileNames().contains(fileName)) {
                clientsWithFile.add(client);
            }
        }
        
        System.out.println("Peer "+ ip+":"+port+" solicitou arquivo "+fileName);

        return clientsWithFile;
    }
    
    /**
     * Atualiza a lista de arquivos de um cliente registrado no servidor.
     *
     * @param ipAddress  Endereço IP do cliente
     * @param port       Porta do cliente
     * @param fileNames  Lista de nomes de arquivos atualizados do cliente
     * @return true se a lista de arquivos do cliente foi atualizada com sucesso, false caso contrário
     * @throws RemoteException em caso de erro de comunicação remota
     */    
    public boolean updateFileList(String ipAddress, int port, List<String> fileNames) throws RemoteException {
        for (ClientInfo clientInfo : clients) {
            if (clientInfo.getIp().equals(ipAddress) && clientInfo.getPort() == port) {
                clientInfo.setFileNames(fileNames);
                return true;
            }
        }
        return false;
    }



    public static void main(String[] args) {
        try {
        	
            Scanner scanner = new Scanner(System.in);
    		System.out.print("Insira o IP do server: ");
    		String ip = scanner.nextLine().trim();
    		if (ip.isEmpty()) {
    			ip = "127.0.0.1";
    		}

    		System.out.print("Insira a porta do server: ");
    		String portInput = scanner.nextLine().trim();
    		int port;
    		if (portInput.isEmpty()) {
    			port = 1099;
    		} else {
    			port = Integer.parseInt(portInput);
    		}
    		
            RemoteServer server = new RemoteServer();
            RemoteServerInterface stub = (RemoteServerInterface) UnicastRemoteObject.exportObject(server, 0);

            // Crie o registro RMI na porta 1099
            Registry registry = LocateRegistry.createRegistry(port);
            
            // Vincule o objeto remoto ao registro
            registry.bind("RemoteServer", stub);

            System.out.println("Servidor pronto para receber conexões de clientes.");
        } catch (Exception e) {
            System.err.println("Erro no servidor: " + e.toString());
            e.printStackTrace();
        }
    }
}

