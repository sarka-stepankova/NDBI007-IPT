# NDBI007 Individual programming task

Graphical simulator of Cormack perfect static hashing method implemented
as an individual programming task for Principles of Data Organisation
(Winter semester 2023/24). 

## Running the application

There are (at least) two options for running this project. Firstly you
need to download the project sources. Then:

1. The easier option is to open the project in an Integrated Development
   Environment (IDE), such as **IntelliJ IDEA** or Eclipse. (I only tested
   it in IntelliJ IDEA). Ensure that your IDE is configured to use **JDK 11**,
   open Main class and run it.
2. Or you can use Apache Maven and run the project with it. You need to have
   Maven and java installed. To ensure that you have JDK 11 you can type `java -version`
   to your terminal. There is pom.xml already configured.
   You only need to run the maven project like that (in linux terminal):
    ```bash
    mvn clean
    mvn install
    mvn exec:java
    ```
