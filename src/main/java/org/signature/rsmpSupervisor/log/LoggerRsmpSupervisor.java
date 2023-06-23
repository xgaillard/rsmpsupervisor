package org.signature.rsmpSupervisor.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

/**
 * Gestionnaire de log de l'application
 * 
 * @author SDARIZCUREN
 *
 */
public class LoggerRsmpSupervisor {
	private final Logger _loggerRsmpSupervisorJsonRecus;
	private final Logger _loggerRsmpSupervisorJsonEmis;
	
	private final Logger _loggerRsmpSupervisorLcrRecus;
	private final Logger _loggerRsmpSupervisorLcrEmis;
	
	private final Logger _loggerRsmpSupervisorDiaserRecus;
	private final Logger _loggerRsmpSupervisorDiaserEmis;

	/**
	 * Construction du gestionnaire depuis sl'injection de dépendances
	 */
	@Inject
	private LoggerRsmpSupervisor() {
		_loggerRsmpSupervisorJsonRecus = LogManager.getLogger("LogRsmpSupervisorJsonRecus");
		_loggerRsmpSupervisorJsonEmis = LogManager.getLogger("LogRsmpSupervisorJsonEmis");
		
		_loggerRsmpSupervisorLcrRecus = LogManager.getLogger("LogRsmpSupervisorLcrRecus");
		_loggerRsmpSupervisorLcrEmis = LogManager.getLogger("LogRsmpSupervisorLcrEmis");
		
		_loggerRsmpSupervisorDiaserRecus = LogManager.getLogger("LogRsmpSupervisorDiaserRecus");
		_loggerRsmpSupervisorDiaserEmis = LogManager.getLogger("LogRsmpSupervisorDiaserEmis");
	}

	/**
	 * Demande le logger à utiliser
	 * 
	 * @param pClass la classe à utiliser
	 * 
	 * @return le logger à utiliser pour les évènements et défauts
	 */
	public synchronized Logger getLoggerEvenements(Class<?> pClass) {
		return LogManager.getLogger(pClass);
	}

	/**
	 * Demande le logger à utiliser pour les données json reçus
	 * 
	 * @return le logger à utiliser pour les sauvegardes des données json reçus
	 */
	public Logger getLoggerJsonRecus() {
		return _loggerRsmpSupervisorJsonRecus;
	}

	/**
	 * Demande le logger à utiliser pour les données json émis
	 * 
	 * @return le logger à utiliser pour les sauvegardes des données json émis
	 */
	public Logger getLoggerJsonEmis() {
		return _loggerRsmpSupervisorJsonEmis;
	}
	
	/**
	 * Demande le logger à utiliser pour les données LCR reçus
	 * 
	 * @return le logger à utiliser pour les sauvegardes des données LCR reçus
	 */
	public Logger getLoggerLcrRecus() {
		return _loggerRsmpSupervisorLcrRecus;
	}

	/**
	 * Demande le logger à utiliser pour les données LCR émis
	 * 
	 * @return le logger à utiliser pour les sauvegardes des données LCR émis
	 */
	public Logger getLoggerLcrEmis() {
		return _loggerRsmpSupervisorLcrEmis;
	}
	
	/**
	 * Demande le logger à utiliser pour les données DIASER reçus
	 * 
	 * @return le logger à utiliser pour les sauvegardes des données DIASER reçus
	 */
	public Logger getLoggerDiaserRecus() {
		return _loggerRsmpSupervisorDiaserRecus;
	}

	/**
	 * Demande le logger à utiliser pour les données DIASER émis
	 * 
	 * @return le logger à utiliser pour les sauvegardes des données DIASER émis
	 */
	public Logger getLoggerDiaserEmis() {
		return _loggerRsmpSupervisorDiaserEmis;
	}
}
