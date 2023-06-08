package org.signature.rsmpSupervisor.communication;

/**
 * Association d'un message texte et d'un identifiant de client
 * 
 * @author SDARIZCUREN
 *
 */
public class MessageClient {
	public final String msg;
	public final String clientId;
	public final boolean erreurCommunication;

	/**
	 * Construction du message
	 * 
	 * @param pMsg le message à transmettre
	 * @param pId  l'id du client
	 */
	public MessageClient(String pMsg, String pId) {
		this(pMsg, pId, false);
	}
	
	/**
	 * Construction du message avec erreur de communication
	 * 
	 * @param pId  l'id du client
	 */
	public MessageClient(String pId) {
		this("", pId, true);
	}
	
	/**
	 * Construction du message
	 * 
	 * @param pMsg le message à transmettre
	 * @param pId  l'id du client
	 * @param pErreur true si problème de communication
	 */
	private MessageClient(String pMsg, String pId, boolean pErreur) {
		msg = pMsg;
		clientId = pId;
		erreurCommunication = pErreur;
	}
}
