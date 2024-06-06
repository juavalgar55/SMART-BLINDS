//#include <HTTPClient.h>
//#include "ArduinoJson.h"
//#include <WiFiUdp.h>
//#include <PubSubClient.h>


#include <WiFi.h>
#include <HTTPClient.h>
#include "ArduinoJson.h"
#include <WiFiUdp.h>
#include <PubSubClient.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <DHT_U.h>
#include <ESP32Servo.h>
// Replace 0 by ID of this current device
const int DEVICE_ID = 33;

int test_delay = 1000; // so we don't spam the API
boolean describe_tests = true;

// Replace 0.0.0.0 by your server local IP (ipconfig [windows] or ifconfig [Linux o MacOS] gets IP assigned to your PC)
String serverName = "http://192.168.18.84:8080/";

HTTPClient http;

// Replace WifiName and WifiPassword by your WiFi credentials
#define STASSID "Cablevision_obisls"    //"Your_Wifi_SSID"
#define STAPSK "UM5U2WvA" //"Your_Wifi_PASSWORD"



// MQTT configuration
WiFiClient espClient;
PubSubClient client(espClient);

// Server IP, where de MQTT broker is deployed
const char *MQTT_BROKER_ADRESS = "192.168.18.84";
const uint16_t MQTT_PORT = 1883;

// Name for this MQTT client
const char *MQTT_CLIENT_NAME = "ArduinoClient_1";

#define DHTPIN 33 // Pin donde está conectado el sensor
#define DHTTYPE DHT11 // Tipo de sensor (DHT11, DHT22, DHT21)
#define DHTPIN2 26 // Pin donde está conectado el sensor

DHT dht(DHTPIN, DHTTYPE);
DHT dht2(DHTPIN2, DHTTYPE);

Servo myservo;  // crea el objeto servo
#define SERVO_PIN 12  // define el pin donde está conectado el servo
// callback a ejecutar cuando se recibe un mensaje
// en este ejemplo, muestra por serial el mensaje recibido
void OnMqttReceived(char *topic, byte *payload, unsigned int length)
{
  int posicionServo;
  Serial.print("Received on ");
  Serial.print(topic);
  Serial.print(": ");

  String content = "";
  for (size_t i = 0; i < length; i++)
  {
    content.concat((char)payload[i]);
  }
  Serial.print(content);
  Serial.println();

  if (content == "ON")
  {
    posicionServo=180;
    myservo.write(posicionServo);  // Mueve el servo a 180 grados (o a la posición que desees)
    Serial.print("MQTT ha encendido el servo");
    delay(5000);
  }
    if (content == "OFF")
  {
    posicionServo=0;
    myservo.write(posicionServo);  // Mueve el servo a 90 grados (o a la posición que desees)
    Serial.print("MQTT ha apagado el servo");
    delay(5000);
  }
}

// inicia la comunicacion MQTT
// inicia establece el servidor y el callback al recibir un mensaje
void InitMqtt()
{
  client.setServer(MQTT_BROKER_ADRESS, MQTT_PORT);
  client.setCallback(OnMqttReceived);
}




// Setup
void setup()
{
  Serial.begin(9600);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(STASSID);

  /* Explicitly set the ESP32 to be a WiFi-client, otherwise, it by default,
     would try to act as both a client and an access-point and could cause
     network-issues with your other WiFi-devices on your WiFi-network. */
  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }
  
  dht.begin(); // Inicializa el sensor DHT
  myservo.attach(SERVO_PIN);  // Inicializa el servo en el pin especificado

  InitMqtt();

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");
}

// conecta o reconecta al MQTT
// consigue conectar -> suscribe a topic y publica un mensaje
// no -> espera 5 segundos
void ConnectMqtt()
{
  Serial.print("Starting MQTT connection...");
  if (client.connect(MQTT_CLIENT_NAME))
  {
    client.subscribe("Grupo1");
    client.publish("Grupo1", "connected");
  }
  else
  {
    Serial.print("Failed MQTT connection, rc=");
    Serial.print(client.state());
    Serial.println(" try again in 5 seconds");

    delay(5000);
  }
}

// gestiona la comunicación MQTT
// comprueba que el cliente está conectado
// no -> intenta reconectar
// si -> llama al MQTT loop
void HandleMqtt()
{
  if (!client.connected())
  {
    ConnectMqtt();
  }
  client.loop();
}

String response;

String serializeSensorValueBody(int idSensor,int idPlaca,long timeStamp,int temperatura,int idGroup)
{
  // StaticJsonObject allocates memory on the stack, it can be
  // replaced by DynamicJsonDocument which allocates in the heap.
  //
  DynamicJsonDocument doc(2048);

  // Add values in the document
  //
  doc["idSensor"] = idSensor;
  doc["idPlaca"] = idPlaca;
  doc["timeStamp"] = timeStamp;
  doc["temperatura"] = temperatura;
  doc["idGroup"] = idGroup;
//  doc["removed"] = false;

  // Generate the minified JSON and send it to the Serial port.
  //
  String output;
  serializeJson(doc, output);
  Serial.println(output);

  return output;
}

String serializeActuatorStatusBody(int idActuador,int idPlaca,bool estado,int grados,long timeStamp,int idGroup)
{
  DynamicJsonDocument doc(2048);

  doc["idActuador"] = idActuador;
  doc["idPlaca"] = idPlaca;
  doc["estado"] = estado;
  doc["grados"] = grados;
  doc["timeStamp"]=timeStamp;
  doc["idGroup"] = idGroup;
 // doc["removed"] = false;

  String output;
  serializeJson(doc, output);
  Serial.println(output);
  return output;
}



void deserializeActuatorStatusBody(String responseJson)
{
  if (responseJson != "")
  {
    DynamicJsonDocument doc(2048);

    // Deserialize the JSON document
    DeserializationError error = deserializeJson(doc, responseJson);

    // Test if parsing succeeds.
    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // Fetch values.
    int idActuador= doc["idActuador"];
    int idPlaca =  doc["idPlaca"];
    bool estado = doc["estado"];
    int grados = doc["grados"];
    long timeStamp = doc["timeStamp"];
    int idGroup=doc["idGroup"];

    Serial.println(("Actuator status deserialized: [idActuator: " + String(idActuador) + ", idPlaca: " + String(idPlaca) + ", estado: " + String(estado) + ", grados" + String(grados) +", timeStamp" + String(timeStamp) + ", idGroup: " + String(idGroup) + "]").c_str());
  }
}



void deserializeSensorsFromDevice(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();
    // allocate the memory for the document
    DynamicJsonDocument doc(ESP.getMaxAllocHeap());

    // parse a JSON array
    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // extract the values
    JsonArray array = doc.as<JsonArray>();
    for (JsonObject sensor : array)
    {
    
    int idSensor= doc["idSensor"];
    int idPlaca =  doc["idPlaca"];
    long timeStamp = doc["timeStamp"];
    int temperatura = doc["temperatura"];
    int idGroup=doc["idGroup"];

      Serial.println(("Sensor deserialized: [idSensor: " + String(idSensor) + ", idPlaca: " + idPlaca +  ", timeStamp: " + String(timeStamp) +", temperatura: " + String(temperatura) +", idGroup: "+String(idGroup)+ "]").c_str());
    }
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void deserializeActuatorsFromDevice(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();
    // allocate the memory for the document
    DynamicJsonDocument doc(ESP.getMaxAllocHeap());

    // parse a JSON array
    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // extract the values
    JsonArray array = doc.as<JsonArray>();
    for (JsonObject sensor : array)
    {
      int idActuador= sensor["idActuador"];
      int idPlaca =  sensor["idPlaca"];
      bool estado = sensor["estado"];
      int grados = sensor["grados"];
      long timeStamp = sensor["timeStamp"];
      int idGroup=sensor["idGroup"];

      Serial.println(("Actuator status deserialized: [idActuator: " + String(idActuador) + ", idPlaca: " + String(idPlaca) + ", estado: " + String(estado) + ", grados" + String(grados) + ", timeStamp" + String(timeStamp) + ", idGroup: " + String(idGroup) + "]").c_str());
    }
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void test_response(int httpResponseCode)
{
  delay(test_delay);
  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String payload = http.getString();
    Serial.println(payload);
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void describe(char *description)
{
  if (describe_tests)
    Serial.println(description);
}
void POST_tests_MainVerticle()
{
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  float h2 = dht2.readHumidity();
  float t2 = dht2.readTemperature();

  // Verifica si hay algún error al leer el sensor
  if (isnan(h) || isnan(t))
  {
      Serial.println("Failed to read from DHT sensor!");
      return;
  }
  if (isnan(h2) || isnan(t2))
 {
      Serial.println("Failed to read from DHT sensor!");
      return;
  }
  int posicionServo;
  int thresholdTemperature = 25; // Define el umbral de temperatura
  if (t > thresholdTemperature || t2 > thresholdTemperature)
  {
      posicionServo=180;
      myservo.write(posicionServo);  // Mueve el servo a 180 grados
      Serial.println("Servo moved to  degrees due to high temperature");
  }
  else
  {
      posicionServo=0;
      myservo.write(posicionServo);  // Mueve el servo a 0 grados
      Serial.println("Servo moved to 0 degrees due to normal temperature");
  }


  Serial.print("Humidity: ");
  Serial.print(h);
  Serial.print(" %\t");
  Serial.print("Temperature: ");
  Serial.print(t);
  Serial.println(" *C");

  Serial.print("Humidity: ");
  Serial.print(h2);
  Serial.print(" %\t");
  Serial.print("Temperature: ");
  Serial.print(t2);
  Serial.println(" *C");

  // Crear cuerpo de la solicitud con los valores del sensor
  String sensor_value_body = serializeSensorValueBody(1, 1, millis(), (int)t, 1);
  String sensor_value_body2 = serializeSensorValueBody(2, 1, millis(), (int)t2, 2);
 
  describe("Test POST with sensor value");
  String serverPath = serverName + "api/sensor/dht";
  http.begin(serverPath.c_str());
  test_response(http.POST(sensor_value_body));
  test_response(http.POST(sensor_value_body2));
    
  String actuator_value_body = serializeActuatorStatusBody(1,1, true, posicionServo, millis(),1);
  describe("Test POST with actuator state");
  serverPath = serverName + "api/actuador/servo";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_value_body));
}

void POST_tests()
{
  String actuator_states_body = serializeActuatorStatusBody(7,7, true, 1, millis(),1);

  describe("Test POST with actuator state");
  String serverPath = serverName + "api/actuador";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));

  String sensor_value_body = serializeSensorValueBody(18,18, millis(), 2000,1);
 
  describe("Test POST with sensor value");
  serverPath = serverName + "api/sensor";
  http.begin(serverPath.c_str());
  test_response(http.POST(sensor_value_body));


}

// Run the tests!
void loop()
{
  delay(2000);
  POST_tests_MainVerticle();
  //POST_tests();
  HandleMqtt();
}
