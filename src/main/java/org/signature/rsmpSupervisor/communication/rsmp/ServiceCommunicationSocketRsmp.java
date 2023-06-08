package org.signature.rsmpSupervisor.communication.rsmp;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.signature.rsmpSupervisor.communication.MessageClient;
import org.signature.rsmpSupervisor.communication.tcp.CommunicationSocketTCP;
import org.signature.rsmpSupervisor.communication.tcp.CommunicationTcpEncapsulationRsmp;
import org.signature.rsmpSupervisor.log.LoggerRsmpSupervisor;

import com.google.inject.Inject;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * CLasse traitant de l'ouverture et fermeture d'une connexion TCP avec un
 * serveur RSMP. Cette classe fait le lien entre les messages reçus et le
 * traitement RSMP à faire
 * 
 * @author SDARIZCUREN
 *
 */
public class ServiceCommunicationSocketRsmp implements Observer<MessageAvecOrigine> {
	private final LoggerRsmpSupervisor _logger;

	private TraitementProtocoleRsmp _serviceRsmp;
	private CommunicationSocketTCP _communicationTcp;

	private List<Disposable> disposable = new ArrayList<>();

	private PublishSubject<MessageClient> _subject;

	/**
	 * Construction de l'objet depuis l'injection de dépendances
	 */
	@Inject
	private ServiceCommunicationSocketRsmp(LoggerRsmpSupervisor pLogger) {
		_logger = pLogger;
	}

	/**
	 * Lancement d'une connexion TCP avec le serveur RSMP, et mise en place de la
	 * messagerie RSMP
	 * 
	 * @param pSocketTravail la socket à utiliser
	 * @param pServiceRsmp   le service RSMP à utiliser
	 * @param pObserver      l'observateur à prévenir des commandes reçues
	 */
	public void lancementCommunicationRsmp(Socket pSocketTravail, TraitementProtocoleRsmp pServiceRsmp,
			Observer<MessageClient> pObserver) {
		_serviceRsmp = pServiceRsmp;
		_serviceRsmp.setObserver(this);

		// Enregistrement de l'observateur
		_subject = PublishSubject.create();
		_subject.subscribe(pObserver);

		_communicationTcp = new CommunicationTcpEncapsulationRsmp(pSocketTravail, this, _logger);
		// Lancement de la communication
		_communicationTcp.start();
	}

	/**
	 * Donne l'identifiant du client connecté
	 * 
	 * @return
	 */
	public String getIdRsmpClient() {
		return _serviceRsmp.getIdRsmpClient();
	}

	/**
	 * Demande de traitement d'une commande extérieure, à traduire en RSMP et à
	 * émettre au client
	 * 
	 * @param pCommande la commande à émettre
	 * @param pIdClient l'id du client à utiliser
	 */
	public void commandePourClient(String pCommande, String pIdClient) {
		if (pCommande == null || pCommande.length() == 0) {
			return;
		}

		_serviceRsmp.commandePourClient(pCommande, pIdClient);
	}

	/**
	 * Indique si la socket TCP est déconnecté
	 * 
	 * @return true si déconnecté
	 */
	public boolean estDeconnecte() {
		return _communicationTcp.estDeconnecte();
	}

	/**
	 * Connection à un observable
	 * 
	 * @param d un objet permettant de mettre fin à la connexion
	 */
	@Override
	public void onSubscribe(@NonNull Disposable d) {
		// J'enregistre l'observable pour mettre fin plus tard à la connexion si besoin
		disposable.add(d);
	}

	/**
	 * Reception d'un message posté par un observable
	 */
	@Override
	public void onNext(@NonNull MessageAvecOrigine msg) {
		switch (msg.origine) {
		case SOCKET_TCP:
			// Nouveau message à analyser par le protocole RSMP
			_serviceRsmp.receptionNouveauMessage(msg.msg);
			break;
		case MESSAGERIE_RSMP:
			// Echange de messages internes par la messagerie RSMP
			_communicationTcp.emissionSocketTcp(msg.msg);
			break;
		case CLIENT_EXTERNE:
			// Message remonté par la messagerie RSMP, qu'il faut passer au client externe
			if (_subject.hasObservers()) {
				_subject.onNext(new MessageClient(msg.msg, getIdRsmpClient()));
			}
			break;
		case INDEFINI:
			// Rien à faire
			break;
		}
	}

	/**
	 * Erreur de communication
	 */
	@Override
	public void onError(@NonNull Throwable e) {
		fermetureToutesCommunications();
	}

	/**
	 * Un observable à mis fin a ses communications
	 */
	@Override
	public void onComplete() {
		fermetureToutesCommunications();

		// Prévient le gestionnaire de communication de ne plus envoyer de commandes
		// Création d'un message d'erreur
		if (_subject.hasObservers()) {
			_subject.onNext(new MessageClient(getIdRsmpClient()));
		}
	}

	// Met fin aux observables et coupe les communications
	private void fermetureToutesCommunications() {
		disposable.forEach(d -> d.dispose());

		disposable = new ArrayList<>();

		_communicationTcp.fermetureSocket();
		_serviceRsmp.forceArretCommunicationRsmp();
	}

}
