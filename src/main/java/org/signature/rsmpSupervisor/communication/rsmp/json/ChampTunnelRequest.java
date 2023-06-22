package org.signature.rsmpSupervisor.communication.rsmp.json;

/**
 * Le champ tunnel d'un message TunnelRequest
 * @author SDARIZCUREN
 *
 */
public class ChampTunnelRequest {
	public String cCI = "M0000";
	public String n = "data";
	public String cO = "sendCommand";
	public String v = "";
	
	public ChampTunnelRequest(String val) {
		v = val;
	}

}
