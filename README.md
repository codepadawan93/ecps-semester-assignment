# ECPS Semester Assignment

## Requirements

Create a Java Card applet using JavaCard 3.1.0+ API for the following requirements (maximum 10 out of 10):

- [x] Encrypt and Decrypt a file (PDF, Word or another format) with AES algorithm (works partially - see below)
- [x] The Java Card application exchanges message and is selected by a host stand-alone application (C/C++/Java), or a browser plugin.
- [x] The source code for the Java card applet and for the host application must be provided.
- [x] Also the files with the compilation phases and the test statements in batch must be provided.

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

## How to run
Open the directory with eclipse, should pick up the projects

## Project Structure
There are 2 projects, the client and the applet. Run the applet in the emulator then run the client in parallel. The encrypted file will be in Output/outfile.enc

## Caveats
Currently it only works for the first APDU sent due to some netcode issues. I could not configure the APIs to work on the client side so I attempted to reverse-engineer the protocol on the raw TCP level with limited success. I got the APDU part right but there is an additional TCP envelope of 6 bytes at the beginning and 2 bytes at the end that I did not fully understand and I could not replicate or find documentation on. Therefore I was unable to fully implement the client functionality. However please note that there are some custom scripts included in the Applet project, such as ```encryptionTest.script``` that demonstrate the Applet works correctly. 

## APDU structure
<table class="wikitable">
   <tbody>
      <tr>
         <th colspan="3">Command APDU</th>
      </tr>
      <tr>
         <th>Field name</th>
         <th>Length (bytes)</th>
         <th>Description</th>
      </tr>
      <tr>
         <td>CLA</td>
         <td>1</td>
         <td>Instruction class - indicates the type of command, e.g. interindustry or proprietary</td>
      </tr>
      <tr>
         <td>INS</td>
         <td>1</td>
         <td>Instruction code - indicates the specific command, e.g. "write data"</td>
      </tr>
      <tr>
         <td>P1-P2</td>
         <td>2</td>
         <td>Instruction parameters for the command, e.g. offset into file at which to write the data</td>
      </tr>
      <tr>
         <td>L<sub>c</sub></td>
         <td>0, 1 or 3</td>
         <td>
            Encodes the number (N<sub>c</sub>) of bytes of command data to follow
            <p>0 bytes denotes N<sub>c</sub>=0<br>
               1 byte with a value from 1 to 255 denotes N<sub>c</sub> with the same value<br>
               3 bytes, the first of which must be 0, denotes N<sub>c</sub> in the range 1 to 65 535 (all three bytes may not be zero)
            </p>
         </td>
      </tr>
      <tr>
         <td>Command data</td>
         <td>N<sub>c</sub></td>
         <td>N<sub>c</sub> bytes of data</td>
      </tr>
      <tr>
         <td>L<sub>e</sub></td>
         <td>0, 1, 2 or 3</td>
         <td>
            Encodes the maximum number (N<sub>e</sub>) of response bytes expected
            <p>0 bytes denotes N<sub>e</sub>=0<br>
               1 byte in the range 1 to 255 denotes that value of N<sub>e</sub>, or 0 denotes N<sub>e</sub>=256<br>
               2 bytes (if extended L<sub>c</sub> was present in the command) in the range 1 to 65 535 denotes N<sub>e</sub> of that value, or two zero bytes denotes 65 536<br>
               3 bytes (if L<sub>c</sub> was not present in the command), the first of which must be 0, denote N<sub>e</sub> in the same way as two-byte L<sub>e</sub>
            </p>
         </td>
      </tr>
      <tr>
         <th colspan="3">Response APDU</th>
      </tr>
      <tr>
         <td>Response data</td>
         <td>N<sub>r</sub> (at most N<sub>e</sub>)</td>
         <td>Response data</td>
      </tr>
      <tr>
         <td>SW1-SW2<br>(Response trailer)</td>
         <td>2</td>
         <td>Command processing status, e.g. 90 00 (<a href="/wiki/Hexadecimal" title="Hexadecimal">hexadecimal</a>) indicates success</td>
      </tr>
   </tbody>
</table>

Source: [Wikipedia](https://en.wikipedia.org/wiki/Smart_card_application_protocol_data_unit)

## Additional references
- [Setup tutorial](https://scc.rhul.ac.uk/files/2016/10/Eclipse.pdf)
- [Comprehensive Java Card Tutorial](http://javacard.vetilles.com/tutorial/)
