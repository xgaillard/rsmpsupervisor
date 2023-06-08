package org.signature.rsmpSupervisor.communication.tcp;

import java.net.Socket;

/**
 * Interface pour être prévenu de nouvelles connexions
 * @author SDARIZCUREN
 *
 */
public interface IConnexionClientTcp {
	
	/**
	 * Informe d'une nouvelle connexion TCP
	 * @param pSocket la socket connectée
	 */
	public void nouvelleConnexionTcp(Socket pSocket);

}
