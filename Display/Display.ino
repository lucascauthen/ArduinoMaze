//LED Matrix Test
#include <Wire.h>
const int DELAY = 10;

int loopCount = 0;

const int SIZE = 16;
char maze[SIZE][SIZE];

const int COLOR_PIN = 6;
bool colorState = LOW;
char color = 'g';
const int HALF_PIN  = 7;
bool halfState = LOW;

const int SINK0     = 8;

void setup() {
  randomSeed(analogRead(0));
  for(int i = 2; i < 20; i++){
    pinMode(i, OUTPUT);
  }
  Serial.begin(9600);
  //randomMap();  
  Wire.begin(4);
  Wire.onReceive(
}

void loop() {
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
  for (int row = 0; row < 16; row++) {
    unsigned char binary[8];
    byte mask = 128;
    byte receivedByte = Wire.read(); //gets first 8 bits of the row
    for(int i = 0; i < 8; i++) { //Converts byte to bits
      binary[i] = ((receivedByte & (mask >> i)) != 0);
    }
    for (int j = 0; j < 8; j++) { //Puts bits into maze array
      maze[row][j] = binary[j];
    }
    receivedByte = Wire.read(); //gets second 8 bits of the row
    for(int i = 0; i < 8; i++) {
      binary[i] = ((receivedByte & (mask >> i)) != 0);
    }
    for (int j = 0; j < 8; j++) { //Puts bits into maze array
      maze[row][j+7] = binary[j];
    }
  }
}

void recieveEvent(int howMany){
  while(Wire.available()){
    byte type = Wire.read();
    switch(type){
      case 0://Start signal
        mazeByteDecoder();
        break;
      case 1://Stop signal
        printMapSerial();
      case 2://new player position
        
      case 3://
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

