# java-refactorer
a tool to help refactoring code across multiple projects/services in java

mvn clean install

java -jar target/refactorer-1.0-SNAPSHOT-jar-with-dependencies.jar core.json  | tsort

core.json to contain the list of directories of your java modules.
It expects `src/` dir to be present for all modules.
