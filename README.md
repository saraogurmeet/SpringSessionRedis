# SpringSessionRedis

This project is using Redis to store session information instead of storing it in web container. Please provide the spring.redis.cluster.nodes property in src/main/resources/application.properties

Use the below command to build the project - 
mvn -e clean install

Once the build is successful, please start the class - SpringSessionRedisApplication

Initial url for login page - http://localhost:8080/app
Login information - user1,user2,user3,...,user20
Password - password
