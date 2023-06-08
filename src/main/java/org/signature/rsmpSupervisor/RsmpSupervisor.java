package org.signature.rsmpSupervisor;

import org.apache.logging.log4j.Level;

/**
 * Classe principale de l'application
 * 
 * @author SDARIZCUREN
 *
 */
public class RsmpSupervisor {
	public static void main(String[] args) {
		new RsmpSupervisor();
	}

	public RsmpSupervisor() {
		// Lancement des services de communication
		FabriqueRsmpSupervisor.INSTANCE.getGestionnaireCommunication().lancementServices();

		FabriqueRsmpSupervisor.INSTANCE.getLoggerRsmpSupervisor().getLoggerEvenements(RsmpSupervisor.class)
				.log(Level.INFO, "Démarrage du service RSMP Supervisor");
	}
}
