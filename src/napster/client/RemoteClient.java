package napster.client;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import napster.server.ClientInfo;
import napster.server.RemoteServerInterface;

/* 
 * Modelo de cliente em uma rede P2P similar ao sistema Napster, permite a conexão de N clientes
 * com um servidor, o qual deve ser executado antes dos clientes. Utiliza conexões RMI para 
 * comunicação com o servidor e conexão TCP para a transferência de arquivos.
 * 
 * Projeto realizado para a disciplina Sistemas Distribuídos - UFABC
 * 
 * @author Lucas Henrique Gois de Campos
 * 
 */
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

			// Recebe a referência para o registro RMI
			Registry registry = LocateRegistry.getRegistry("localhost", 1099);

			// Recebe o objeto remoto do servidor pelo nome
			RemoteServerInterface server = (RemoteServerInterface) registry.lookup("RemoteServer");

			Scanner scanner = new Scanner(System.in);
			boolean exit = false;
			boolean joined = false;

			while (!exit) {
				System.out.println("\nMenu:");
				System.out.println("1. JOIN");
				System.out.println("2. SEARCH");
				System.out.println("3. DOWNLOAD");
				System.out.println("4. UPDATE");
				System.out.println("0. Sair");
				System.out.print("Digite a opção desejada: ");
				int option = scanner.nextInt();
				scanner.nextLine();

				switch (option) {
				case 1: // Opção JOIN
					joined = server.join(client.ip, client.port, client.fileNames);

					if (joined) {
						// Cria o ServerSocket para receber solicitações de download
						ServerSocket fileServerSocket = new ServerSocket(client.port);
						System.out.println("JOIN_OK"); 
						System.out.println("Sou peer"+client.ip+":"+client.port+" com arquivos "+Arrays.toString(client.fileNames.toArray()));
						// Aguarda solicitações de download
						handleFileRequest(fileServerSocket,client);
						break;

					} else {
						System.out.println("Cliente já conectado");
						break;
					}

				case 2: // Opção SEARCH
					System.out.print("Digite o nome do arquivo a ser pesquisado: ");
					String fileName = scanner.nextLine();

					List<ClientInfo> clientsWithFile = server.search(client.ip,client.port,fileName);

					if (clientsWithFile.isEmpty()) {
						System.out.println("Nenhum cliente possui o arquivo.");
					} else {
						System.out.println("Peers com o arquivo solicitado:");
						for (ClientInfo clientInfo : clientsWithFile) {
							System.out.println("[" + clientInfo.getIp() + ":" + clientInfo.getPort()+"]");
						}
					}
					break;
				case 3: // Opção DOWNLOAD
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
				case 4: // Opção UPDATE
				    if (!joined) {
				        System.out.println("Você precisa fazer JOIN antes de atualizar a lista de arquivos.");
				        break;
				    }
				    updateFileList(server, client);
				    break;

					
				case 0: // Opção SAIR
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

	/**
	 * Cria um objeto RemoteClient a partir das informações fornecidas pelo usuário.
	 *
	 * @return Um objeto RemoteClient
	 */
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

		// Identifica os arquivos na pasta indicada
		File folder = new File(folderName);
		File[] files = folder.listFiles();

		List<String> fileNames = new ArrayList<>();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					fileNames.add(file.getName());
				}
			}
		}

		return new RemoteClient(ip, port, folderName, fileNames);
	}

	/**
	 * Solicita o download de um arquivo de um cliente remoto.
	 *
	 * @param ip          Endereço IP do cliente remoto
	 * @param port        Porta do cliente remoto
	 * @param fileName    Nome do arquivo a ser baixado
	 * @param folderName  Pasta onde o arquivo será salvo
	 */
	private static void requestFile(String ip, int port, String fileName, String folderName) {
		Thread requestThread = new Thread(() -> {			

			try (Socket socket = new Socket(ip, port)) {

				OutputStream os = socket.getOutputStream();
				DataOutputStream writer = new DataOutputStream(os);
				writer.writeBytes(fileName+"\n");

				InputStream is = socket.getInputStream();
				DataInputStream dis = new DataInputStream(is);

				long fileSize = dis.readLong();
				if (fileSize > 0) {
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
					System.out.println("\n Arquivo "+fileName+" baixado com sucesso na pasta " + folderName);
				} else {
					System.out.println("\n O arquivo não está disponível para download.");
				}
				socket.close();
			} catch (IOException e) {
				System.out.println("\n Erro ao solicitar o arquivo: " + e.getMessage());
			}
		});

		requestThread.start();
	}


	/**
	 * Lida com as solicitações de arquivos recebidas de outros clientes.
	 *
	 * @param fileServerSocket  ServerSocket responsável por receber as solicitações de download
	 * @param client            Cliente remoto
	 */
	private static void handleFileRequest(ServerSocket fileServerSocket, RemoteClient client) {
		Thread handleThread = new Thread(() -> {
			while(true) {
				try {
					Socket socket = fileServerSocket.accept();

					InputStream is = socket.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(is));

					// Receber o nome do arquivo solicitado
					String fileName = br.readLine();

					OutputStream os = socket.getOutputStream();
					DataOutputStream dos = new DataOutputStream(os);

					// Verifica se o arquivo solicitado existe na pasta do cliente
					File fileToSend = new File(client.folderName + File.separator + fileName);
					
					if (fileToSend.exists()) {
						long fileSize = fileToSend.length();

						// Envia o tamanho do arquivo para o cliente solicitante
						dos.writeLong(fileSize);

						// Envia o arquivo para o cliente solicitante em blocos de 4096 bytes
						FileInputStream fis = new FileInputStream(fileToSend);
						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = fis.read(buffer)) != -1) {
							dos.write(buffer, 0, bytesRead);
						}
						fis.close();
						socket.close();
						
					} else {
						// Se o arquivo não existe, enviar tamanho 0
						dos.writeLong(0);
						System.out.println("\n O arquivo não existe ou não está disponível para download.");
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
	
	/**
	 * Atualiza a lista de arquivos do cliente no servidor.
	 *
	 * @param server Objeto remoto do servidor
	 * @param client Cliente remoto
	 */
	private static void updateFileList(RemoteServerInterface server, RemoteClient client) {
	    try {
	    	
	    	// Obtém a lista de arquivos da pasta do cliente
			File folder = new File(client.folderName);
			File[] files = folder.listFiles();

			List<String> fileNames = new ArrayList<>();
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						fileNames.add(file.getName());
					}
				}
			}
			
			// Chama o método remoto do servidor para atualizar a lista de arquivos do cliente
	        boolean updated = server.updateFileList(client.ip, client.port, fileNames);
	        if (updated) {
	            System.out.println("UPDATE_OK.");
	        } else {
	            System.out.println("Falha ao atualizar a lista de arquivos.");
	        }
	    } catch (RemoteException e) {
	        System.out.println("Erro de comunicação com o servidor: " + e.getMessage());
	    }
	}


}

