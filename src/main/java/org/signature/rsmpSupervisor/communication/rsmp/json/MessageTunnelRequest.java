package org.signature.rsmpSupervisor.communication.rsmp.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe MessageTunnelRequest à mapper en JSON
 * @author SDARIZCUREN
 *
 */
public class MessageTunnelRequest extends MessageRsmp implements IdentifiantMessage {
	
	public String mType = "rSMsg";
	public String mId = "";
	public String ntsOId;
	public String xNId = "";
	public String cId;
	public List<ChampTunnelRequest> arg;
	
	public final static String TYPE_MESSAGE = "CommandRequest";
	
	public MessageTunnelRequest(String pMsgId, String pTxt, String pIdClient) {
		super(TYPE_MESSAGE);
		
		mId = pMsgId;
		ntsOId = pIdClient;
		cId = pIdClient;
		
		arg = new ArrayList<>();
		arg.add(new ChampTunnelRequest(pTxt));
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
