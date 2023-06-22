package napster.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import napster.server.RemoteServerInterface;

public class RemoteClient {
	public static void main(String[] args) {
		try {
			// Obtenha a referência para o registro RMI
			Registry registry = LocateRegistry.getRegistry("localhost", 1099);

			// Obtenha o objeto remoto do servidor pelo nome
			RemoteServerInterface server = (RemoteServerInterface) registry.lookup("RemoteServer");

			// Solicita ao usuário que digite um comando
			Scanner scanner = new Scanner(System.in);
			System.out.print("Digite um comando ('JOIN' para se juntar): ");
			String command = scanner.nextLine();

			 // Verifica o comando digitado
            if (command.equalsIgnoreCase("JOIN")) {
                // Envia uma mensagem "JOIN" e o endereço IP para o servidor
                String ipAddress = "192.168.0.1"; // Substitua pelo endereço IP do cliente
                boolean joined = server.join(ipAddress);

                if (joined) {
                    System.out.println("Cliente conectado com sucesso.");
                } else {
                    System.out.println("Falha ao conectar o cliente.");
                }
			} else {
				System.out.println("Comando inválido.");
			}

			scanner.close();
		} catch (Exception e) {
			System.err.println("Erro no cliente: " + e.toString());
			e.printStackTrace();
		}
	}
}

