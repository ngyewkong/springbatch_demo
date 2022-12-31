Before running the Spring Boot Application:
- add application.properties/.yml file in the src/resources dir
- containing the database connectivity credentials (using MySQL as eg)
  - spring.datasource.url=jdbc:mysql://{db_server_ip/localhost}:{port_number}/{schema_name} 
  - spring.datasource.username= 
  - spring.datasource.password=