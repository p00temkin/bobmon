## BobMon

Provides mobile notification (using Pushover, https://pushover.net/) when bobcat miner needs a forced resync. 

![alt text](https://github.com/p00temkin/bobmon/blob/master/img/bobmont1.png?raw=true)

### Prerequisites

   ```
git clone https://github.com/p00temkin/forestfish.git
mvn clean package install
   ```

### Building the application

   ```
   mvn clean package
   mv target/bobmon-0.0.1-SNAPSHOT-jar-with-dependencies.jar bobmon.jar
   ```
   
### Usage

Using a Bobcat Miner with IP 192.168.1.1 and a pushover UserID A and AppID B.

   ```
   java -jar ./bobmon.jar -b "http://192.168.1.1/status.json" -u A -a B
   ```
   
Options:
   ```
 -a,--apitokenappid <arg>           API token app ID 
 -u,--apitokenuserid <arg>          API token user ID
 -b,--bobcatstatusurl <arg>         URL to get the status from the Bobcat Miner
   ```
   
### Support/Donate

forestfish.x, 0x207d907768Df538F32f0F642a281416657692743
   
