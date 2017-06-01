//LED Matrix Test

const int DELAY = 10;

int loopCount = 0;

const int SIZE = 16;
char grid[SIZE][SIZE];

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
  randomMap();  
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
  delayMicroseconds(DELAY);
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
    if(color == grid[column][i]){
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
        grid[i][j] = 'r';
      } else if(number == 1){
        grid[i][j] = 'g';
      } else{
        grid[i][j] = ' ';
      }
      Serial.print(grid[i][j]);
    }
    Serial.println();
  }
}

