# ECPS Semester Assignment

## Requirements

Create a Java Card applet using JavaCard 3.1.0+ API for the following requirements (maximum 10 out of 10):

- [ ] Encrypt and Decrypt a file (PDF, Word or another format) with AES algorithm.
- [ ] The Java Card application exchanges message and is selected by a host stand-alone application (C/C++/Java), or a browser plugin.
- [ ] The source code for the Java card applet and for the host application must be provided.
- [ ] Also the files with the compilation phases and the test statements in batch must be provided.

## Configuration
### Prerequisites:
1. [Eclipse 3.3.1](https://archive.eclipse.org/eclipse/downloads/drops/R-3.3.1-200709211145/)
2. [JCDE 0.1 plugins](https://osdn.net/projects/sfnet_eclipse-jcde/downloads/eclipse-jcde/eclipse-jcde-0.1/eclipse-jcde-0.1.zip/)
3. [JRE 1.5 (v49.0)](https://www.oracle.com/java/technologies/java-archive-javase5-downloads.html)
4. [Java Card Kit 2.2.2](https://www.oracle.com/java/technologies/java-archive-downloads-javame-downloads.html#java_card_kit-2.2.2-oth-JPR)
### Setup
1. Install Eclipse 3.3.1 portable
2. Install JCDE 0.1 plugins in <eclipse folder>/plugins/
3. Install JRE 1.5 portable in <eclipse folder>/jre/ and set JAVA_HOME environment variable to this path
4. Unzip java_card_kit-2_2_2 somewhere
5. In Eclipse, go to Java Card > Preferences > Java Card Home and set it to the path you installed java_card_kit-2_2_2 at 
6. In Eclipse, go to File > New > Other and select Java Card Project
7. Happy coding!
