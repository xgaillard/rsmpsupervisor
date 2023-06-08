package org.signature.rsmpSupervisor.communication.lcr;

import java.net.Socket;

import org.apache.logging.log4j.Level;
import org.signature.rsmpSupervisor.communication.GestionnaireCommunication;
import org.signature.rsmpSupervisor.communication.IServicePostReponseRsmp;
import org.signature.rsmpSupervisor.communication.rsmp.MessageAvecOrigine;
import org.signature.rsmpSupervisor.communication.tcp.CommunicationTcpEncapsulationTedi;
import org.signature.rsmpSupervisor.communication.tcp.IConnexionClientTcp;
import org.signature.rsmpSupervisor.communication.tcp.ServeurTcp;
import org.signature.rsmpSupervisor.log.LoggerRsmpSupervisor;

import com.google.inject.Inject;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Un service chargé de la communication en LCR avec un Superviseur externe
 * 
 * @author SDARIZCUREN
 *
 */
public class ServiceCommunicationLcr implements IConnexionClientTcp, Observer<MessageAvecOrigine>, IServicePostReponseRsmp {
	private final ServeurTcp _serveurTcp;
	private final LoggerRsmpSupervisor _logger;

	private String _idClient = "";

	private GestionnaireCommunication _gestionnairePrincipal;

	private CommunicationTcpEncapsulationTedi _comTedi;

	/**
	 * Construction de l'objet depuis l'injection de dépendances
	 */
	@Inject
	private ServiceCommunicationLcr(ServeurTcp pServeurTcp, LoggerRsmpSupervisor pLogger) {
		_serveurTcp = pServeurTcp;
		_logger = pLogger;
	}

	/**
	 * Démarrage des services de communication
	 * 
	 * @param pGestionnairePrincipal le gestionnaire à prévenir d'une nouvelle trame
	 *                               LCR reçue
	 * @param pTcpPort               le port TCP à écouter
	 * @param pIdClient              l'id du client associé au port TCP
	 */
	public void lancementServices(GestionnaireCommunication pGestionnairePrincipal, int pTcpPort, String pIdClient) {
		_gestionnairePrincipal = pGestionnairePrincipal;
		_idClient = pIdClient;

		_serveurTcp.lancementService(this, pTcpPort, false, "", "");
	}
	
	/**
	 * Retourne l'id RSMP associé
	 * 
	 * @return l'id RSMP
	 */
	@Override
	public String getIdClientRsmp() {
		return _idClient;
	}

	/**
	 * Informe d'une nouvelle connexion TCP
	 * 
	 * @param pSocket la socket connectée
	 */
	@Override
	public synchronized void nouvelleConnexionTcp(Socket pSocket) {
		// Communication TEDI-LCR. Je traite une seule connexion à la fois
		_comTedi = new CommunicationTcpEncapsulationTedi(pSocket, this, _logger);
		_comTedi.start();

	}

	/**
	 * Demande à poster la réponse reçue sur la couche RSMP
	 * 
	 * @param pReponse la réponse reçue
	 */
	public void postReponseRsmp(String pReponse) {
		// Je retourne la réponse au système à l'origine de la demande
		_comTedi.emissionSocketTcp(pReponse);

		// Log de la trame
		_logger.getLoggerLcrEmis().log(Level.DEBUG, "LCR --> " + pReponse);
	}

	/**
	 * Connection à un observable
	 * 
	 * @param d un objet permettant de mettre fin à la connexion
	 */
	@Override
	public void onSubscribe(@NonNull Disposable d) {

	}

	/**
	 * Reception d'un message posté par un observable
	 */
	@Override
	public void onNext(@NonNull MessageAvecOrigine msg) {
		// Je transmet la commande au gestionnaire principal de communication qui va la
		// transmettre à la couche RSMP
		_gestionnairePrincipal.traitementCommandeExterne(msg.msg, _idClient);

		// Log de la trame
		_logger.getLoggerLcrRecus().log(Level.DEBUG, "LCR --> " + msg.msg);
	}

	/**
	 * Erreur de communication
	 */
	@Override
	public void onError(@NonNull Throwable e) {
	}

	/**
	 * Un observable à mis fin a ses communications
	 */
	@Override
	public void onComplete() {
	}
}
