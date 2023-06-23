package napster.client;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import napster.server.ClientInfo;
import napster.server.RemoteServerInterface;

public class RemoteClient {
	
    private String ip;
	private int port;
	private List<String> fileNames;
	
	public RemoteClient(String ip, int port, String folderName, List<String> fileNames) {
        this.ip = ip;
        this.port = port;
        this.fileNames = fileNames;
    }

    
    public static void main(String[] args) {
        try {
        	
        	RemoteClient client = createClient();
        	
            // Obtenha a referência para o registro RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // Obtenha o objeto remoto do servidor pelo nome
            RemoteServerInterface server = (RemoteServerInterface) registry.lookup("RemoteServer");

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n Menu:");
                System.out.println("1. JOIN");
                System.out.println("2. SEARCH");
                System.out.println("0. Sair");
                System.out.print("Digite a opção desejada: ");
                int option = scanner.nextInt();
                scanner.nextLine(); // Limpar o buffer

                switch (option) {
                    case 1:
                 
                    	boolean joined = server.join(client.ip, client.port, client.fileNames);

                        if (joined) {
                            System.out.println("Cliente conectado com sucesso. \n");
                        } else {
                            System.out.println("Falha ao conectar o cliente. \n");
                        }
                        break;
                    case 2:
                        System.out.print("Digite o nome do arquivo a ser pesquisado: ");
                        String fileName = scanner.nextLine();

                        List<ClientInfo> clientsWithFile = server.search(fileName);

                        if (clientsWithFile.isEmpty()) {
                            System.out.println("Nenhum cliente possui o arquivo.");
                        } else {
                            System.out.println("Clientes que possuem o arquivo " + fileName + ":");
                            for (ClientInfo clientInfo : clientsWithFile) {
                                System.out.println("IP: " + clientInfo.getIp() + ", Porta: " + clientInfo.getPort());
                            }
                        }
                        break;

                    case 0:
                        exit = true;
                        break;
                    default:
                        System.out.println("Opção inválida.");
                        break;
                }
            }

            scanner.close();
        } catch (Exception e) {
            System.err.println("Erro no cliente: " + e.toString());
            e.printStackTrace();
        }
    }
    
    private static RemoteClient createClient() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Insira o IP: ");
        String ip = scanner.nextLine().trim();
        if (ip.isEmpty()) {
            ip = "127.0.0.1";
        }

        System.out.print("Insira a porta: ");
        String portInput = scanner.nextLine().trim();
        int port;
        if (portInput.isEmpty()) {
            port = 1099;
        } else {
            port = Integer.parseInt(portInput);
        }

        System.out.print("Insira a pasta: ");
        String folderName = scanner.nextLine().trim();

        // Identificar arquivos na pasta indicada
        File folder = new File(folderName);
        File[] files = folder.listFiles();

        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileNames.add(file.getName());
                    System.out.println(file.getName());
                }
            }
        }

        return new RemoteClient(ip, port, folderName, fileNames);
    }
}


