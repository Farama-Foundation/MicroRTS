mkdir build
javac -d "./build" -cp "./lib/*" -sourcepath "./src" ./src/tests/*.java ./src/tests/sockets/*.java
cd build
jar cvf microrts.jar *
mv microrts.jar ../microrts.jar
cd ..