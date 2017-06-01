#include <Wire.h>
const int SIZE = 16;
bool maze[16][16];
int path = 1;
int wall = 0;
int xMax = 15;
int yMax = 15;
int yMin = 0;
int xMin = 0;

byte playerX = 0;
byte playerY = 0;
byte last_playerX = 0;
byte last_playerY = 0;

const int refreshInterval = 60;

unsigned long lastRefreshTime;
byte input_buffer[20];
int lastIndex = 0;
bool inputComplete = true;
byte curDirection = 0; //None

const int COLOR_PIN = 6;
bool colorState = LOW;
char color = 'g';
const int HALF_PIN  = 7;
bool halfState = LOW;

const int SINK0  = 8;

const int DELAY = 50;

int loopCount = 0;

void setup() {
  Serial.begin(9600);
  Wire.begin();
  randomSeed(analogRead(0)); //Seeds the random generator with a random seed
  for(int i = 2; i < 20; i++){
    pinMode(i, OUTPUT);
  }
}

void loop() {
  unsigned long now = millis();

  if ((now - lastRefreshTime) > refreshInterval) {
    if(Serial.available() > 0) {
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
    updateCoordinates();
    lastRefreshTime = now;
  }

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

void handleInput() {
  switch(input_buffer[0]) {
    case 0: //Start singal
      mazeCreator(getDifficultyFromByte(input_buffer[1]));
      encode_to_rg();
      mazePrinter();
      break;
    case 1: //Stop signal
      setBlankMap();
      break;
    case 2: //New input signal
      //Wire.write(2);
      byte inDirection = input_buffer[1];
      curDirection = inDirection;
      Serial.print("New Direction: ");
      switch(inDirection) {
        case 0: Serial.print("None"); break;
        case 1: Serial.print("Up"); break;
        case 2: Serial.print("Down"); break;
        case 3: Serial.print("Left"); break;
        case 4:  Serial.print("Right"); break;
      }
      Serial.println();
      break;
  }
}


void updateCoordinates() {
  byte newDirection = 0;
  switch(curDirection) {
    case 0: //None
      //Do nothing
      break;
    case 1: //Up
      if(canMoveUp()) {
        playerY += 1;
        newDirection = 1;
      }
      break;
    case 2: //Down
      if(canMoveDown()) {
        playerY -= 1;
        newDirection = 2;
      }
      break;
    case 3: //Left
      if(canMoveLeft()) {
        playerY -= 1;
        newDirection = 3;
      }
      break;
    case 4: //Right
      if(canMoveRight()) {
        playerX += 1;
        newDirection = 4;
      }
      break;
  }
  if(newDirection != 0) {
    maze[last_playerX][last_playerY] = ' ';
    maze[playerX][playerY] = 'g';
  }
}

void encode_to_rg() {
  for(int i = 0; i < 16; i++) {
    for(int j = 0; j < 16; j++) {
      if(maze[i][j] == 1) {
        maze[i][j] = 'r';
      } else {
        maze[i][j] = ' ';
      }
    }
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

boolean gameWon(){
  if (playerY == yMax){
    return true;
  }
  return false;
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
    Serial.println(current);
    Wire.write(current); //sends byte
    current = 0;
    for (int j = 8; j < 16; j++){ //Goes through last 8 columns
      current +- maze[i][j] * pow(2, 15-j);
    }
    Serial.println(current);
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
