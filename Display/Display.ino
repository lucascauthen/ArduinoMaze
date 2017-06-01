//LED Matrix Test
#include <Wire.h>
const int DELAY = 50;

int loopCount = 0;

const int SIZE = 16;
char maze[SIZE][SIZE];

const int COLOR_PIN = 6;
bool colorState = LOW;
char color = 'g';
const int HALF_PIN  = 7;
bool halfState = LOW;

const int SINK0  = 8;
byte playerX = 0;
byte playerY = 0;

void setup() {
  randomSeed(analogRead(0));
  for(int i = 2; i < 20; i++){
    pinMode(i, OUTPUT);
  }
  Serial.begin(9600);
  //randomMap();  
  Wire.begin(4);
  Wire.onReceive(receiveEvent);
}

void loop() {
  //Serial.print("loop");
  if(colorState == LOW){
    color = 'r';
  } else {
    color = 'g';
  }
  digitalWrite(COLOR_PIN, colorState);
  digitalWrite(HALF_PIN, halfState);
  for(int i = 0; i < SIZE; i++){
    writeColumn(i, color);
  }

  colorState = !colorState;
  //delayMicroseconds(DELAY);
  //delay(DELAY);
  if((loopCount + 1) % 2 == 0){
    halfState = !halfState;
  }
  loopCount++;
}

void writeColumn(int column, char color){
  for(int i = 0; i < 4; i++){
    digitalWrite(i + 2, bitRead(column, i));
  }
  
  for(int i = 0; i < (SIZE / 2); i++){
    if(color == maze[column][i]){
      digitalWrite(SINK0 + i, HIGH);
    } else {
      digitalWrite(SINK0 + i, LOW);
    }
    
  }
//  delayMicroseconds(DELAY);
  delay(DELAY);
  //Reset phase
//  for(int i = 0; i < (SIZE / 2); i++){
//    digitalWrite(SINK0 + i, HIGH);
//  }
  /*
  for(int i = 2; i < COLOR_PIN; i++){
    digitalWrite(i, LOW);
  }*/
}

void randomMap(){
  for(int i = 0; i < SIZE; i++){
    for(int j = 0; j < SIZE; j++){
      int number = random(3);
      if(number == 2) {
        maze[i][j] = 'r';
      } else if(number == 1){
        maze[i][j] = 'g';
      } else{
        maze[i][j] = ' ';
      }
      Serial.print(maze[i][j]);
    }
    Serial.println();
  }
}

void setBlankMap(){
  for(int i = 0; i < SIZE; i++){
    for(int j = 0; j < SIZE; j++){
      maze[i][j] = ' ';
    }
  }
}

void mazeByteDecoder() { //Decodes bytes send from the data Arduino and creates an array
  for (int col = 0; col < 16; col++) {
    unsigned char binary[8];
    byte mask = 128;
    byte receivedByte = Wire.read(); //gets first 8 bits of the row
    Serial.println(receivedByte);
    for(int i = 0; i < 8; i++) { //Converts byte to bits
      binary[i] = ((receivedByte & (mask >> i)) != 0);
    }
    for (int j = 0; j < 8; j++) { //Puts bits into maze array
      maze[j][col] = binary[j];
    }
    receivedByte = Wire.read(); //gets second 8 bits of the row
    Serial.println(receivedByte);
    for(int i = 0; i < 8; i++) {
      binary[i] = ((receivedByte & (mask >> i)) != 0);
    }
    for (int j = 0; j < 8; j++) { //Puts bits into maze array
      maze[j+7][col] = binary[j];
    }
  }
}

void receiveEvent(int howMany){
  Serial.println("Received data");
  while(Wire.available()){
    byte type = Wire.read();
    Serial.println(type);
    switch(type){
      case 0://Start signal
        mazeByteDecoder();
        decode_2();
        printMapSerial();
        break;
      case 1://Stop signal
        setBlankMap();
        printMapSerial();
        break;
      case 2://new player position
        int playerXNew = Serial.read();
        int playerYNew = Serial.read();
        maze[playerX][playerY] = ' ';
        maze[playerXNew][playerYNew] = 'g';
        Serial.print("px: ");
        Serial.print(playerX);
        Serial.print(" py: ");
        Serial.print(playerY);
        Serial.println();
        break;
    }
  }
}

void decode_2() {
  for(int i = 0; i < 16; i++) {
    for(int j = 0; j < 16; j++) {
      if(maze[i][j] == 1) {
        maze[i][j] = 'g';
      } else {
        maze[i][j] = 'r';
      }
    }
  }
}

void printMapSerial(){
  for(int i = 0; i < SIZE; i++){
    for(int j = 0; j < SIZE; j++){
      Serial.print(maze[i][j]);
    }
    Serial.println();
  }
}

