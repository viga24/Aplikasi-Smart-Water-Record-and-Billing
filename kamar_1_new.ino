//Library
#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>

#include <NTPClient.h>
#include <WiFiUdp.h>

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org");

//Set WiFi
#define WIFI_SSID "INET"
#define WIFI_PASSWORD "internetan"

//Set Firestore
#define API_KEY "AIzaSyD9pDPXU963CHl6gFDm3E6xIzGrk5BqsVs"
#define FIREBASE_PROJECT_ID "smartkost-4e188"
#define USER_EMAIL "user@gmail.com"
#define USER_PASSWORD "user123"

//Firestore
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

//Flow1
#define SENSOR  D2
 
long currentMillis = 0;
long previousMillis = 0;
int interval = 1000;
boolean ledState = LOW;
float calibrationFactor = 4.5;
volatile byte pulseCount;
byte pulse1Sec = 0;
float flowRate;
unsigned long flowMilliLitres;
unsigned int totalMilliLitres;
float flowLitres;
float totalLitres;
 
unsigned long lastConnectionTime = 0;           // last time you connected to the server, in milliseconds
const unsigned long postingInterval = 5 * 1000;

//Deklarasi
String id_kamar = "2";
unsigned long dataMillis = 0;

//Fungsi Firestore
void fcsUploadCallback(CFS_UploadStatusInfo info)
{
  if (info.status == firebase_cfs_upload_status_init){
    Serial.printf("\nProses kirim (%d)...\n", info.size);
  } else if (info.status == firebase_cfs_upload_status_upload) {
    Serial.printf("Terkirim %d%s\n", (int)info.progress, "%");
  } else if (info.status == firebase_cfs_upload_status_complete){
    Serial.println("Kirim selesai ");
  } else if (info.status == firebase_cfs_upload_status_process_response) {
    Serial.print("Mengambil respon... ");
  } else if (info.status == firebase_cfs_upload_status_error){
    Serial.printf("Gagal, %s\n", info.errorMsg.c_str());
  }
}

void IRAM_ATTR pulseCounter()
{
  pulseCount++;
}

void setup()
{
  //Serial
  Serial.begin(9600);
  
  //Koneksi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Menyambungkan ke Wi-Fi");
  unsigned long ms = millis();
  while (WiFi.status() != WL_CONNECTED){
    Serial.print(".");
    delay(300);
  }

  timeClient.begin();
  timeClient.setTimeOffset(25200);

  Serial.println();
  Serial.println("Berhasil!");
  Serial.printf("Firebase Client v%s\n\n", FIREBASE_CLIENT_VERSION);

  //Flow1
  pinMode(SENSOR, INPUT_PULLUP);
 
  pulseCount = 0;
  flowRate = 0.0;
  flowMilliLitres = 0;
  totalMilliLitres = 0;
  previousMillis = 0;
 
  attachInterrupt(digitalPinToInterrupt(SENSOR), pulseCounter, FALLING);
  
  //Firestore
  config.api_key = API_KEY;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;
  config.token_status_callback = tokenStatusCallback; 
  Firebase.reconnectNetwork(true);
  fbdo.setBSSLBufferSize(4096, 1024 );
  fbdo.setResponseSize(2048);
  Firebase.begin(&config, &auth);
}

void loop()
{
  timeClient.update();

  time_t epochTime = timeClient.getEpochTime();
  struct tm *ptm = gmtime ((time_t *)&epochTime); 

  char dateBuffer[12];
  sprintf(dateBuffer,"%04u-%02u-%02u", ptm->tm_year+1900,ptm->tm_mon+1,ptm->tm_mday);

  String timestamp = dateBuffer;
  String formattedTime = timeClient.getFormattedTime();
  
  timestamp += " " + formattedTime;
  
  //Random Dokumen
  const char characters[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  const int stringLength = 20;
  char randomString[stringLength + 1];
  for (int i = 0; i < stringLength; i++) {
    int randomIndex = random(sizeof(characters) - 1);
    char randomChar = characters[randomIndex];
    randomString[i] = randomChar;
  }
  randomString[stringLength] = '\0';
  
  //Firestore
  FirebaseJson content;
  String documentPath = "Record_air/" + String(randomString);

  currentMillis = millis();
  if (currentMillis - previousMillis > interval) 
  {
    
    pulse1Sec = pulseCount;
    pulseCount = 0;
 
    // Because this loop may not complete in exactly 1 second intervals we calculate
    // the number of milliseconds that have passed since the last execution and use
    // that to scale the output. We also apply the calibrationFactor to scale the output
    // based on the number of pulses per second per units of measure (litres/minute in
    // this case) coming from the sensor.
    flowRate = ((1000.0 / (millis() - previousMillis)) * pulse1Sec) / calibrationFactor;
    previousMillis = millis();
 
    // Divide the flow rate in litres/minute by 60 to determine how many litres have
    // passed through the sensor in this 1 second interval, then multiply by 1000 to
    // convert to millilitres.
    flowMilliLitres = (flowRate / 60) * 1000;
    flowLitres = (flowRate / 60);
 
    // Add the millilitres passed in this second to the cumulative total
    totalMilliLitres += flowMilliLitres;
    totalLitres += flowLitres;
    
    // Print the flow rate for this second in litres / minute
    Serial.print("Flow rate: ");
    Serial.print(float(flowRate));  // Print the integer part of the variable
    Serial.print("L/min");
    Serial.print("\t");       // Print tab space
 
    // Print the cumulative total of litres flowed since starting
    Serial.print("Output Liquid Quantity: ");
    Serial.print(totalMilliLitres);
    Serial.print("mL / ");
    Serial.print(totalLitres);
    Serial.println("L");
 
  }

  if ((millis() - lastConnectionTime > postingInterval) && totalLitres > 0 && flowRate == 0) {

    if (Firebase.ready()){
      //Variabel
      content.set("fields/air/doubleValue", totalLitres);
      content.set("fields/hari/stringValue", timestamp);
      content.set("fields/id_kamar/integerValue", 1);
      content.set("fields/status/integerValue", 0);

      totalMilliLitres = 0;
      totalLitres = 0;

      //Kirim data
      Serial.print("Mengirim data[1]...");
      if (Firebase.Firestore.createDocument(&fbdo, FIREBASE_PROJECT_ID, "", documentPath.c_str(), content.raw())){
        Serial.printf("ok\n%s\n\n", fbdo.payload().c_str());
      } else {
        Serial.println(fbdo.errorReason());
      }
    }

    lastConnectionTime = millis();
  }




  
  // //Baca Liter1
  // currentTime = millis();
  // if (currentTime >= (cloopTime + 1000)){
  //   cloopTime = currentTime; 
  //   if (flow_frequency != 0){
  //     l_minute = (flow_frequency * volume_per_pulse * 60);
  //     vol = vol + l_minute / 60;
  //     Serial.print("Vol1:");
  //     Serial.print(vol);
  //     Serial.println(" L, ");
  //     flow_frequency = 0;

  //     //Kirim Data
  //     if (Firebase.ready()){
  //       //Variabel
  //       content.set("fields/air/doubleValue", vol);
  //       content.set("fields/hari/stringValue", timestamp);
  //       content.set("fields/id_kamar/integerValue", 1);
  //       content.set("fields/status/integerValue", 0);
    
  //       //Kirim data
  //       Serial.print("Mengirim data[1]...");
  //       if (Firebase.Firestore.createDocument(&fbdo, FIREBASE_PROJECT_ID, "", documentPath.c_str(), content.raw())){
  //         Serial.printf("ok\n%s\n\n", fbdo.payload().c_str());
  //       } else {
  //         Serial.println(fbdo.errorReason());
  //       }
  //     }
  //   } else{
  //     Serial.println("Rate1:0 L/M ");
  //   }
  // }

}
