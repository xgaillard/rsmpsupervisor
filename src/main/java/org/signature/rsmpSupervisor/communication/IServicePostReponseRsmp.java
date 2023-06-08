package org.signature.rsmpSupervisor.communication;

/**
 * Description d'un service capable de poster à un système externe la réponse
 * reçue sur la couche RSMP
 * 
 * @author SDARIZCUREN
 *
 */
public interface IServicePostReponseRsmp {

	/**
	 * Retourne l'id RSMP associé
	 * 
	 * @return l'id RSMP
	 */
	public String getIdClientRsmp();

	/**
	 * Demande à poster la réponse reçue sur la couche RSMP
	 * 
	 * @param pReponse la réponse reçue
	 */
	public void postReponseRsmp(String pReponse);

}
