package napster.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/* 
 * Interface do servidor de uma rede P2P similar ao sistema Napster.
 * 
 * Projeto realizado para a disciplina Sistemas Distribuídos - UFABC
 * 
 * @author Lucas Henrique Gois de Campos
 * 
 */

public interface RemoteServerInterface extends Remote {
	
	/**
     * Método para que um cliente se conecte ao servidor.
     *
     * @param ipAddress Endereço IP do cliente
     * @param port Porta do cliente
     * @param fileNames Lista de nomes de arquivos do cliente
     * @return true se o cliente foi conectado com sucesso, false se o cliente já estava conectado
     * @throws RemoteException em caso de erro de comunicação remota
     */
    boolean join(String ipAddress, int port, List<String> fileNames) throws RemoteException;
    
    /**
     * Método para realizar a busca de um arquivo no servidor.
     *
     * @param ipAddress Endereço IP do cliente
     * @param port Porta do cliente
     * @param fileName Nome do arquivo a ser buscado
     * @return Lista de ClientInfo dos clientes que possuem o arquivo
     * @throws RemoteException em caso de erro de comunicação remota
     */
    List<ClientInfo> search(String ip, int port, String fileName) throws RemoteException;
    
    /**
     * Atualiza a lista de arquivos de um cliente registrado no servidor.
     *
     * @param ipAddress  Endereço IP do cliente
     * @param port       Porta do cliente
     * @param fileNames  Lista de nomes de arquivos atualizados do cliente
     * @return true se a lista de arquivos do cliente foi atualizada com sucesso, false caso contrário
     * @throws RemoteException em caso de erro de comunicação remota
     */
    boolean updateFileList(String ipAddress, int port, List<String> fileNames) throws RemoteException;

}
