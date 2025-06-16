# Graphql java en full SQL

Projet de démo pour faire du grapqhl avec du SQL (postgresql) à 100% plutôt que d'utiliser JPA. 

Ce projet utilise : 
* springboot-graphql : pour le moteur graphql  
* jooq : pour le requêtage à la base de données

3 approches dont testées : 
* requête un premier niveau, puis complêter avec `@SchemaMapping` 
* requête un premier niveau, puis complêter avec `@BatchMapping` 
* Tout faire en SQL en 1 seule requête

