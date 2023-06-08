package org.signature.rsmpSupervisor.communication.rsmp.json;

/**
 * Classe Watchdog à mapper en JSON
 * @author SDARIZCUREN
 *
 */
public class MessageWatchdog extends MessageRsmp implements IdentifiantMessage {
	public String mType = "rSMsg";
	public String mId = "";
	public String wTs = "";
	
	public final static String TYPE_MESSAGE = "Watchdog";
	
	public MessageWatchdog(String pMsgId, String pTimeStamp) {
		super(TYPE_MESSAGE);
		mId = pMsgId;
		wTs = pTimeStamp;
		
	}
	
	/**
	 * Donne l'UUID du message
	 * 
	 * @return son identifiant
	 */
	public String getIdentifiantMessage() {
		return mId;
	}
	

}
