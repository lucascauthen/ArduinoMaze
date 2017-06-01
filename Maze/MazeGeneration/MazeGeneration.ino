#include <Wire.h>
bool maze[16][16];
int path = 1;
int wall = 0;
int xMax = 15;
int yMax = 15;
int yMin = 0;
int xMin = 0;

int playerX = 0;
int playerY = 0;

const int refreshInterval = 60;

unsigned long lastRefreshTime;
byte input_buffer[20];
int lastIndex = 0;
bool inputComplete = true;

void setup() {
  Serial.begin(9600);
  Wire.begin();
  randomSeed(analogRead(0)); //Seeds the random generator with a random seed
}

void loop() {
  unsigned long now = millis();

  if ((now - lastRefreshTime) > refreshInterval) {
    if(Serial.available() > 0) {
      Serial.println("Reading...");
      if(inputComplete) {
        input_buffer[lastIndex++] = Serial.read();
        inputComplete = false;
      } else {
        input_buffer[lastIndex] = Serial.read();
        if(input_buffer[lastIndex] == 255) {
          inputComplete = true;
          handleInput();
          lastIndex = 0;
        } else {
          lastIndex++;
        }
      }
    }
    lastRefreshTime = now;
  }

}

void handleInput() {
  Wire.beginTransmission(4);
  switch(input_buffer[0]) {
    case 0: //Start singal
      mazeCreator(getDifficultyFromByte(input_buffer[1]));
      Wire.write(0);
      mazePrinter();
      //mazeByteEncoder(); //Write the encoded map
      break;
    case 1: //Stop signal
      Wire.write(1); //tell it to clear the screen
      break;
    case 2: //New input signal
      Wire.write(2);
      byte inDirection = input_buffer[1];
      updateCoordinates(inDirection);
      break;
  }
  Wire.endTransmission();
}

void updateCoordinates(byte inDirection) {
  switch(inDirection) {
    case 0: //None

      break;
    case 1: //Up

      break;
    case 2: //Down

      break;
    case 3: //Left

      break;
    case 4: //Right

      break;
  }
}

int getDifficultyFromByte(byte in) {
  Serial.println(in);
  switch(in) {
    case 0:
      return 32;
    case 1:
      return 48;
    case 2:
      return 64;
  }
}

//Initializes maze given a difficulty
void mazeCreator(int difficulty) { //difficulty ranges from 32 to 64, 32 being easiest, 64 being hardest
  for (int i = 0; i < 16; i++){
    for (int j = 0; j < 16; j++){
      maze[i][j] = path; //set all points on the grid to walls
    }
  }
  int wallCount = 0;  //Number of walls constructed
  while (wallCount < difficulty){ //while maze still neds to be made, find random square and see if it can be made into a path
    int i = random(15);
    int j = random(15);
    if (!isEdge(i, j)){
      if(mazeGeneration(i,j)){ //ensures that the square can be changed
        wallCount++;
      }
    }
  }
  for (int i = 0; i < 15; i++){
      int x = random(2);
      int x2 = random(2);
      maze[i][xMin] = x;
      maze[i][xMax] = x2;
  }
}

//Breaks down walls of maze, current maze square is [i][j]
boolean mazeGeneration(int i, int j) {
  //If square is surrounded by paths, make that square a wall
 if (maze[i-1][j] == path && maze[i][j+1] == path && maze[i][j-1] == path && maze[i+1][j] == path &&
        maze[i][j] == path){
    maze[i][j] = wall;
    return true;
 }else {
    return false;
 }
}

boolean isEdge(int i, int j){ //returns true if the given coordinate is an edge and false otherwise
  if (i == xMin || i == xMax){
    return true;
  }else if (j == yMin || j == yMax){
    return true;
  }
  return false;
}

boolean isValidMove(int i, int j){ //takes the coordinates of the next move and determines if it is a wall
  if (maze[i][j] == 0){
    return false;
  }
  return true;
}

boolean canMoveUp() { //Determines if the player can move up one square
  if (isEdge(playerX + 1, playerY + 1)){
    return false;
  }
  if (!isValidMove(playerX, playerY + 1)){
    return false;
  }
  return true;
}

boolean canMoveDown() { //Determines if the player can move down one square
  if (isEdge(playerX + 1, playerY + 1)){
    return false;
  }
  if (!isValidMove(playerX, playerY - 1)){
    return false;
  }
  return true;
}

boolean canMoveLeft() { //Determines if the player can move left one square 
  if (isEdge(playerX + 1, playerY + 1)){
    return false;
  }
  if (!isValidMove(playerX - 1, playerY)){
    return false;
  }
  return true;
}

boolean canMoveRight() { //Determines if the player can move right one square 
  if (isEdge(playerX + 1, playerY + 1)){
    return false;
  }
  if (!isValidMove(playerX + 1, playerY + 1)){
    return false;
  }
  return true;
}

void mazePrinter(){ //prints the contents of the maze
 for (int i = 0; i < 16; i++){
    for (int j = 0; j < 16; j++){
      Serial.print(maze[i][j]);
    }
    Serial.println();
  }
}

void mazeByteEncoder(){ //Sends contents of the maze over I2C by encoding them to bytes.
  byte current = 0;
  for (int i = 0; i < 16; i++){ //Goes through all 16 rows
    current = 0;
    for (int j = 0; j < 8; j++){ //Goes through first 8 columns
      current += maze[i][j] * pow(2, 7-j);
    }
    Wire.write(current); //sends byte
    current = 0;
    for (int j = 8; j < 16; j++){ //Goes through last 8 columns
      current +- maze[i][j] * pow(2, 15-j);
    }
    Wire.write(current); //sends byte
  }
}

void mazeByteDecoder() { //Decodes bytes send from the data Arduino and creates an array
  char mazeDecode[16][16];
  for (int row = 0; row < 16; row++) {
    unsigned char binary[8];
    byte mask = 128;
    byte receivedByte = Wire.read(); //gets first 8 bits of the row
    for(int i = 0; i < 8; i++) { //Converts byte to bits
      binary[i] = ((receivedByte & (mask >> i)) != 0);
    }
    for (int j = 0; j < 8; j++) { //Puts bits into maze array
      mazeDecode[row][j] = binary[j];
    }
    receivedByte = Wire.read(); //gets second 8 bits of the row
    for(int i = 0; i < 8; i++) {
      binary[i] = ((receivedByte & (mask >> i)) != 0);
    }
    for (int j = 0; j < 8; j++) { //Puts bits into maze array
      mazeDecode[row][j+7] = binary[j];
    }
  }
}
