package org.signature.rsmpSupervisor.communication.rsmp.json;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



/**
 * Classe Version à mapper en JSON
 * @author SDARIZCUREN
 *
 */
public class MessageVersion extends MessageRsmp implements IdentifiantMessage {
	public String mType = "rSMsg";
	public String mId;
	public List<ChampVersion> RSMP;
	public List<ChampSiteId> siteId;
	public String SXL;
	
	public final static String TYPE_MESSAGE = "Version";
	
	public MessageVersion(String pMsgId, String pSiteId, List<String> pVersionsRsmp, String pVersionSxl) {
		super(TYPE_MESSAGE);
		mId = pMsgId;
		
		siteId = new ArrayList<>(); 
		siteId.add(new ChampSiteId(pSiteId));
		
		RSMP = pVersionsRsmp.stream().map(s -> new ChampVersion(s)).collect(Collectors.toList());
		
		SXL = pVersionSxl;
		
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
