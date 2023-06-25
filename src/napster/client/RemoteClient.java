package napster.client;

import java.io.*;
import java.net.*;
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
	private String folderName;

	public RemoteClient(String ip, int port, String folderName, List<String> fileNames) {
		this.ip = ip;
		this.port = port;
		this.folderName = folderName;
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
			boolean joined = false;

			while (!exit) {
				System.out.println("\nMenu:");
				System.out.println("1. JOIN");
				System.out.println("2. SEARCH");
				System.out.println("3. DOWNLOAD");
				System.out.println("0. Sair");
				System.out.print("Digite a opção desejada: ");
				int option = scanner.nextInt();
				scanner.nextLine(); // Limpar o buffer

				switch (option) {
				case 1:
					joined = server.join(client.ip, client.port, client.fileNames);

					if (joined) {
						// Crie o ServerSocket para receber solicitações de download
						ServerSocket fileServerSocket = new ServerSocket(client.port);
						System.out.println("JOIN_OK"); 

						// Aguardar solicitação de download 
						handleFileRequest(fileServerSocket,client);
						break;

					} else {
						System.out.println("Cliente já conectado");
						break;
					}

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
				case 3:
					System.out.print("Digite o endereço IP do cliente para download: ");
					String ipDownload = scanner.nextLine().trim();
					if (ipDownload.isEmpty()) {
						System.out.println("Endereço IP não informado.");
						break;
					}

					System.out.print("Digite a porta do cliente para download: ");
					String portDownload = scanner.nextLine().trim();
					if (portDownload.isEmpty()) {
						System.out.println("Porta não informada.");
						break;
					}

					System.out.print("Digite o nome do arquivo: ");
					String fileNameDownload = scanner.nextLine().trim();
					if (fileNameDownload.isEmpty()) {
						System.out.println("Nome do arquivo não informado.");
						break;
					}


					requestFile(ipDownload,Integer.parseInt(portDownload), fileNameDownload, client.folderName);

					break;
				case 0:
					exit = true;
					break;
				default:
					System.out.println("Opção inválida.");
					break;
				}
			}

			//fileServerSocket.close();
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
			port = 9000;
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

	private static void requestFile(String ip, int port, String fileName, String folderName) {
		Thread requestThread = new Thread(() -> {			

			try (Socket socket = new Socket(ip, port)) {

				OutputStream os = socket.getOutputStream();
				DataOutputStream writer = new DataOutputStream(os);
				writer.writeBytes(fileName+"\n");

				InputStream is = socket.getInputStream();
				DataInputStream dis = new DataInputStream(is);

				long fileSize = dis.readLong();
				System.out.println(fileSize);
				if (fileSize > 0) {
					System.out.println("Iniciando download");
					File fileToReceive = new File(folderName + File.separator + fileName);
					FileOutputStream fos = new FileOutputStream(fileToReceive);

					byte[] buffer = new byte[4096];
					int bytesRead;
					long totalBytesRead = 0;

					while (totalBytesRead < fileSize && ((bytesRead = is.read(buffer)) != -1)) {
						fos.write(buffer, 0, bytesRead);
						totalBytesRead += bytesRead;
					}

					fos.close();
					System.out.println("Download concluído. Arquivo salvo em: " + fileToReceive.getAbsolutePath());
				} else {
					System.out.println("O arquivo não está disponível para download.");
				}
				socket.close();
			} catch (IOException e) {
				System.out.println("Erro ao solicitar o arquivo: " + e.getMessage());
			}
		});

		requestThread.start();
	}



	private static void handleFileRequest(ServerSocket fileServerSocket, RemoteClient client) {
		Thread handleThread = new Thread(() -> {
			while(true) {
				try {
					Socket socket = fileServerSocket.accept();
					System.out.println("Clientes conectados com sucesso.");

					System.out.println("Criando o reader");
					InputStream is = socket.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(is));

					System.out.println("Lendo o nome do arquivo");
					// Receber o nome do arquivo solicitado
					String fileName = br.readLine();
					System.out.println("Solicitação de download recebida para o arquivo: " + fileName);

					OutputStream os = socket.getOutputStream();
					DataOutputStream dos = new DataOutputStream(os);

					// Verificar se o arquivo existe
					File fileToSend = new File(client.folderName + File.separator + fileName);
					System.out.println(fileToSend);
					if (fileToSend.exists()) {
						System.out.println("Iniciar envio");
						long fileSize = fileToSend.length();

						// Enviar tamanho do arquivo
						dos.writeLong(fileSize);

						// Enviar o arquivo em blocos de 4096 bytes
						FileInputStream fis = new FileInputStream(fileToSend);
						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = fis.read(buffer)) != -1) {
							dos.write(buffer, 0, bytesRead);
						}
						fis.close();
						socket.close();
						System.out.println("Arquivo enviado com sucesso.");
					} else {
						// Se o arquivo não existe, enviar tamanho 0
						dos.writeLong(0);
						System.out.println("O arquivo não existe ou não está disponível para download.");
					}

					br.close();
					dos.close();
					//fileServerSocket.close();

				} catch (IOException e) {
					System.out.println("Erro ao lidar com a solicitação de arquivo: " + e.getMessage());
				}
			}
		});

		handleThread.start();

	}

}

