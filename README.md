Projet RsmpSupervisor

La branche principale du projet est la branche master.
Clone du projet : 
	git clone --branch master https://gitlab.com/stephen.darizcuren/rsmpsupervisor.git .
	
Lancement : java -jar RsmpSupervisor-0.3.0.jar

L'application peut être testée avec le simulateur RSMP, ou le client RsmpEquipment-0.1.0.jar

Compilation du projet :
	Il faut disposer de maven sur son poste. 
	A la racine du projet : mvn package. Le fichier jar généré dans le dossier target et à repositionner à la racine du projet
	

Le dossier resources contient le fichier de configuration de l'application. A noter que la version sxl supportée est vide pour permettre les tests avec le simulateur qui ne gère pas par défaut de version sxl.

Le dossier sauvegardeDatas contient des logs tournants (sauvegarde 30j) des données JSON émis et reçus

Version 0.1.0 :
	Emission du message version, du message watchdog, gestion des acquittements
	Emission d'un message de commande JSON. Exemple : {"mType":"rSMsg","mId":"9943e14e-552e-4924-952e-f7f66c129544","mTxt":"DT","type":"TransmissionTexte"}
	Deconnexion sur non réception d'acquittement
	Accepte plusieurs connections simultanées d'équipements RSMP. Chaque socket client est repéré par son id RSMP
	
	La partie connexion avec une supervsision externe (serveur TCP ou UDP) n'est pas développée. Les fonctions d'accroche, récupération d'une commande externe et retour de la réponse, sont néanmoins déjà présentes, dans la classe GestionnaireCommunication.
	
	Il manque également les tests unitaires qui sont donc à écrire
	
Version 0.2.0 :
	Remplacement des appels directs entre les couches par une communication Observable-Observer avec utilisation de la librairie rxjava
	
Version 0.3.0 :
	Ajout communication LCR
	
	
	
Description du projet
	- package org.signature.rsmpSupervisor
		Contient la classe main, et une fabrique permettant l'injection de dépendances
	- package org.signature.rsmpSupervisor.log et org.signature.rsmpSupervisor.resources
		Gestion des logs et du fichier de configuration
	- package org.signature.rsmpSupervisor.communication
		Le singleton GestionnaireCommunication lance le serveur TCP d'attente de connexions RSMP, branche un nouveau client RSMP sur un service de traitement RSMP (emission/reception et gestion du protocole), redirige une demande externe (Supervision LCR, Diaser, ...) associé à un id de client, vers le traitement RSMP associé à ce client, et retourne la reponse reçue sur la couche RSMP vers la supervision externe.
	- package org.signature.rsmpSupervisor.communication.lcr
		Service de communication LCR avec la Supervision externe
	- package org.signature.rsmpSupervisor.communication.tcp
		Classe ServeurTcp : attend une nouvelle connexion d'un client
		Classe CommunicationSocketTcp : classe abstraire pour lire et écrire sur la socket TCP
		Classe CommunicationTcpEncapsulationRsmp et CommunicationTcpEncapsulationTedi : classes étendant CommunicationSocketTcp pour spécifier le comportement en lecture et écriture selon le protocole utilisé
	- package org.signature.rsmpSupervisor.communication.rsmp
		La classe ServiceCommunicationSocketRsmp est le point d'entrée de la couche RSMP. Elle gère la communication sur la socket, et la traduction des messages en RSMP via la classe TraitementProtocoleRSMP
		La classe TraitementProtocoleRsmp s'occupe du parsing des messages JSON, de la génération du message version au démarrage, des messages watchdog, des acquittements, et du transfert du message texte (question LCR/Diaser et réponse équipement)
		La classe HorodateMessageRsmp associe un id de message à un horodate, permettant ainsi le controle de la non réception d'un acquittement et la fermeture de la connexion 
	- package org.signature.rsmpSupervisor.communication.rsmp.json
		Contient les classes à parser en JSON, ou à initialiser à partir d'une chaine JSON

	
		
