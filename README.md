# Dockerization des Micro-Services

# Introduction
Grâce aux conteneurs, vous n’avez pas besoin de développer et de configurer entièrement un nouveau serveur physique, ni de mettre sur pied
un nouvel environnement virtuel, ce qui requiert une émulation de processeur, un système d’exploitation et des logiciels installés.

# Architecture du projet

Voici l'architecture du notre projet:

  [![N|Solid](https://liliasfaxi.github.io/TP-eServices/img/tp4/archi.png)](https://nodesource.com/products/nsolid)
  
- Product Service : Il permet de lister des produits en offrant un web service REST.
- Config Service :  Il contient la configuration de tous les micro services.
- Proxy Service : C'est une passerelle se chargeant du routage d'une requête vers l'une des instances d'un service.
- Discovery Service : Service permettant l'enregistrement des instances de services.

# Compiler Docker images 

Pour ce faire, on va générer un ficher appelé DockerFile dans la racine du chaque projet.

   
  ##### Exemple du DockerFile du projet config-service :
  
   ```sh
   
FROM openjdk:8-jdk-alpine
VOLUME /tmp
EXPOSE 8888
ADD /target/config-service-0.0.1-SNAPSHOT.jar //
CMD	java -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -jar /config-service-0.0.1-SNAPSHOT.jar

   ```
   
 - On definit:  
	- FROM: L'image de base à partir de laquelle on va construire nos conteneurs.
	- VOLUME: Le point de montage à créer. 
	- EXPOSE: Le port réseau. 
	- ADD: Les fichiers, les répertoires ou les URLs des fichiers à ajouter au système de fichiers de l'image.  
	- CMD: Les commandes Shell à excécuter pour executer nos conteneurs.
	

 - On ajoute ces lignes au fichier pom.xml du projet concerné.


```sh
	<properties>
		<docker.image.prefix>config</docker.image.prefix>
	</properties>
	
	<plugins>
	<plugin>
		<plugin>
			<groupId>com.spotify</groupId>
			<artifactId>dockerfile-maven-plugin</artifactId>
			<version>1.3.6</version>
			<configuration>
				<repository>${docker.image.prefix}/${project.artifactId}</repository>
				<buildArgs>
					<JAR_FILE>target/${project.build.finalName}.jar</JAR_FILE>
				</buildArgs>
			</configuration>
		</plugin>
		</plugins>
```
 
 - On Compile l'image 
```sh
     mvn package install dockerfile:build 
```
 - On répete les mêmes etapes pour chaque service et on vérifie la bonne création du chaque conteneur.
```sh
  docker images
```

# Exécuter les conteneurs
  On a deux méthodes :
  1 - Exécuter séparement chaque contenaire à l'aide des commandes.
  2 - Créer un fichier docker-compose qui permet de définir et exécuter nos contenaires.
  
  
  #### 1ére méthode

- On exécute le contenaire de config. Pour ce faire on met le projet dans notre répertoire github.
 ```sh
docker run -it -p 8888:8888 \ config-service \              
      --spring.cloud.config.server.git.uri=https://github.com/NessrineBenZineb/spring-boot-docker\
 ```
 
- On a besoin de savoir l'adresse ip du conteneur ainsi les autres services peuvent trouver le config grâce à cette adresse.
```sh
CONFIG_CONTAINER_ID=`docker ps -a | grep config-service | cut -d " " -f1`
CONFIG_IP_ADDRESS=`docker inspect -f "{{ .NetworkSettings.IPAddress }}" $CONFIG_CONTAINER_ID`
```
    
 - On lance discovery-service 
```sh
docker run -it -p 8761:8761 discovery-service \ -e --spring.cloud.config.uri=http://$CONFIG_IP_ADDRESS:8888
```
 - On lance proxy-service
```sh
docker run -it -p 9999:9999 proxy-service
```
 - On lance product-service 
```sh
docker run -it -p 8080:8080 product-service -e --spring.cloud.config.uri=http://$CONFIG_IP_ADDRESS:8888
```

On ajoute un fichier bootstrap.yml dans chaque service.


```sh
spring:
  cloud:
    config:
      failFast: true
      retry:
        initialInterval: 3000
        multiplier: 1.3
        maxInterval: 5000
        maxAttempts: 20
```
####  2éme méthode

 - On crée le fichier docker-compose.

```sh
version: '2.0'
services:
 config-service:
    image: config-service
    ports:
        - "8888:8888"
    networks:
        - my-network
 
 discovery-service:
    image: discovery-service
    ports: 
        - "8761:8761"
    depends_on: 
        - config-service
    networks:
        - my-network

 product-service:
    image: product-service
    ports: 
        - "8080:8080"
    depends_on: 
        - config-service
    networks:
        - my-network
proxy-service:
    image: proxy-service
    ports:
        - "8888:8888"
    depends_on:
        - config-service
    networks:
        - my-network
networks:
    my-network:
       driver: bridge
```
Au niveau desquels on definit:  
	- Nos services web déja définis dans notre répertoire.   
	- Un réseau "my-network" de type **Bridge** pour notre application. 
	- La dépendance des services au service "config-service".
	
 - On ajoute le fichier application.yml dans le projet config et le fichier bootstrap.yml dans les autres services.
		
 - On exécute ses commandes:
```sh
  docker-compose build
  docker-compose up
```





