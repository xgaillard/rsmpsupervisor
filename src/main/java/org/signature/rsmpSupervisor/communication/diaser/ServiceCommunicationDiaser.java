package org.signature.rsmpSupervisor.communication.diaser;

import org.apache.logging.log4j.Level;
import org.signature.rsmpSupervisor.communication.GestionnaireCommunication;
import org.signature.rsmpSupervisor.communication.IServicePostReponseRsmp;
import org.signature.rsmpSupervisor.communication.udp.ClientServeurUdp;
import org.signature.rsmpSupervisor.communication.udp.IConnexionClientUdp;
import org.signature.rsmpSupervisor.log.LoggerRsmpSupervisor;
import org.signature.rsmpSupervisor.resources.ConfigurationRsmpSupervisor;

import com.google.inject.Inject;

/**
 * Un service chargé de la communication en LCR avec un Superviseur externe
 * 
 * @author SDARIZCUREN
 *
 */
public class ServiceCommunicationDiaser implements IConnexionClientUdp, IServicePostReponseRsmp {
	private final ClientServeurUdp _clientServeurUdp;
	private final LoggerRsmpSupervisor _logger;

	private String _idClient = "";

	private GestionnaireCommunication _gestionnairePrincipal;

	/**
	 * Construction de l'objet depuis l'injection de dépendances
	 */
	@Inject
	private ServiceCommunicationDiaser(ClientServeurUdp pClientServeurUdp, LoggerRsmpSupervisor pLogger,
			ConfigurationRsmpSupervisor pConfiguration) {
		_clientServeurUdp = pClientServeurUdp;
		_logger = pLogger;
	}

	/**
	 * Démarrage des services de communication
	 * 
	 * @param pGestionnairePrincipal le gestionnaire à prévenir d'une nouvelle trame
	 *                               LCR reçue
	 * @param pUdpPort               le port UDP à écouter
	 * @param pIdClient              l'id du client associé au port UDP
	 */
	public void lancementServices(GestionnaireCommunication pGestionnairePrincipal, int pUdpPort, String pIdClient) {
		_gestionnairePrincipal = pGestionnairePrincipal;
		_idClient = pIdClient;

		_clientServeurUdp.lancementService(this, pUdpPort);
	}

	/**
	 * Retourne l'id RSMP associé
	 * 
	 * @return l'id RSMP
	 */
	public String getIdClientRsmp() {
		return _idClient;
	}

	/**
	 * Informe de la réception de données sur le port UDP
	 * 
	 * @param pDatas les données reçues
	 */
	@Override
	public void receptionDatas(String pDatas) {
		// Je transmet la commande au gestionnaire principal de communication qui va la
		// transmettre à la couche RSMP
		_gestionnairePrincipal.traitementCommandeExterne(pDatas, _idClient);

		// Log de la trame
		_logger.getLoggerDiaserRecus().log(Level.DEBUG, "DIASER --> " + pDatas);
	}

	/**
	 * Demande à poster la réponse reçue sur la couche RSMP
	 * 
	 * @param pReponse la réponse reçue
	 */
	public void postReponseRsmp(String pReponse) {
		// Je retourne la réponse au système à l'origine de la demande
		_clientServeurUdp.emissionTrame(pReponse);

		// Log de la trame
		_logger.getLoggerDiaserEmis().log(Level.DEBUG, "DIASER --> " + pReponse);
	}
}
