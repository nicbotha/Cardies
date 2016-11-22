# Cardies
This application sends out Cards (birthday, Christmas etc) based on user uploaded templates. 

Clean install using Maven
'mvn clean install -Dspring.profiles.active="test"'

HCP Local
'mvn clean install neo-java-web:deploy-local -Dspring.profiles.active="test"'
'mvn neo-java-web:start-local'

Integration Test
Goto the web project directory
'mvn clean install -P integration-test -Dspring.profiles.active="dev"'

Start MongoDB locally
mongod.exe -dbpath [path]

### Features 
- Single-Table multi-tenancy
- Entity field validation with I18N
- DBUnit tests running with Derby
- Spring profiles to switch between test/dev/prod datasources
- Uploaded and manage template static content via UI5 application

### Technologies & Libraries
- JPA EclipseLink 
- Spring Data
- Hibernate Validator
- DBUnit
- Freemarker
- Olingo V4.0
