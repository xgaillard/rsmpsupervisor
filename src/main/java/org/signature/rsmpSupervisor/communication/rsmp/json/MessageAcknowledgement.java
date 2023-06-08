package org.signature.rsmpSupervisor.communication.rsmp.json;

/**
 * Classe  Acknowledgement à mapper en JSON
 * @author SDARIZCUREN
 *
 */
public class MessageAcknowledgement extends MessageRsmp {
	public String mType = "rSMsg";
	public String oMId = "";
	
	public final static String TYPE_MESSAGE = "MessageAck";
	
	public MessageAcknowledgement(String pMsgId) {
		super(TYPE_MESSAGE);
		oMId = pMsgId;
	}
	

}
