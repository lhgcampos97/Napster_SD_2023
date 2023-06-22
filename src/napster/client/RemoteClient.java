package napster.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

import napster.server.RemoteServerInterface;

public class RemoteClient {
    public static void main(String[] args) {
        try {
            // Obtenha a referência para o registro RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // Obtenha o objeto remoto do servidor pelo nome
            RemoteServerInterface server = (RemoteServerInterface) registry.lookup("RemoteServer");

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("Menu:");
                System.out.println("1. JOIN");
                System.out.println("2. SEARCH");
                System.out.println("0. Sair");
                System.out.print("Digite a opção desejada: ");
                int option = scanner.nextInt();
                scanner.nextLine(); // Limpar o buffer

                switch (option) {
                    case 1:
                        System.out.print("Digite o endereço IP: ");
                        String ipAddress = scanner.nextLine();
                        boolean joined = server.join(ipAddress);

                        if (joined) {
                            System.out.println("Cliente conectado com sucesso.");
                        } else {
                            System.out.println("Falha ao conectar o cliente.");
                        }
                        break;
                    case 2:
                        List<String> clientList = server.search();
                        System.out.println("Clientes conectados:");
                        for (String clientIP : clientList) {
                            System.out.println(clientIP);
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
}


