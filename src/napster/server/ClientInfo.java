package napster.server;

import java.io.Serializable;
import java.util.List;

/* 
 * Classe cliente utilizada pelo servidor para armazenar as informações dos clientes da rede.
 * 
 * Projeto realizado para a disciplina Sistemas Distribuídos - UFABC
 * 
 * @author Lucas Henrique Gois de Campos
 * 
 */

public class ClientInfo implements Serializable {
	/**
     * Serial version UID para garantir consistência na serialização/desserialização dos objetos.
     */
	private static final long serialVersionUID = 4499890955037846892L;
	private String ip;
    private int port;
    private List<String> fileNames;

    public ClientInfo(String ip, int port, List<String> fileNames) {
        this.ip = ip;
        this.port = port;
        this.fileNames = fileNames;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public List<String> getFileNames() {
        return fileNames;
    }
    
    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }
}

