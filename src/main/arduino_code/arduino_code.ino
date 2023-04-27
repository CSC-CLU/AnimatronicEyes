//  Animatronic Eyes Project by the California Lutheran University Computer Science Club (CSC@CLU)
//  Based on the code of Nilheim Mechatronic's Simplified Eye Mechanism
//  Make sure you have the Adafruit servo driver library installed >>>>> https://github.com/adafruit/Adafruit-PWM-Servo-Driver-Library


#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>

Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();

#define SERVOMIN 140  // this is the 'minimum' pulse length count (out of 4096)
#define SERVOMAX 520  // this is the 'maximum' pulse length count (out of 4096)

// our servo # counter
uint8_t servonum = 0;

int xval;
int yval;

int lexpulse;
int rexpulse;

int leypulse;
int reypulse;

int uplidpulse;
int lolidpulse;
int altuplidpulse;
int altlolidpulse;

int trimval;
int trimval2;

int sensorValue = 0;
int outputValue = 0;
int switchval = 0;

const byte READY = 0x52; // Ready
const byte ACK = 0x06; // Acknowledge
const byte NAK = 0x15; // Negative Acknowledge

// Semantic Versioning
const int8_t majorVersion = 0;
const int8_t minorVersion = 5;
const int8_t patchVersion = 0;
const char* versionDate = __DATE__ " " __TIME__;

void setup() {
  Serial.begin(9600); // For communication with computer
  Serial1.begin(9600); // For debug messages and other use

  // Print version information
  Serial1.print(F("\nAnamatronic eyes project v"));
  Serial1.print(majorVersion);
  Serial1.print('.');
  Serial1.print(minorVersion);
  Serial1.print('.');
  Serial1.println(patchVersion);
  Serial1.print(F("Compiled on "));
  Serial1.println(versionDate);

  Serial1.println(F("8 channel Servo test!"));
  pwm.begin();
  pwm.setPWMFreq(60);  // Analog servos run at ~60 Hz updates
  delay(10);

  Serial1.println(F("Initializing eyes."));
  initializeEyes();

  Serial1.println(F("Ready!"));
  Serial.write(READY);
}

byte* bytes = new byte[100];

void loop() {
  if (Serial.available() > 2) {
    byte command = Serial.read();
    byte checksum = command;
    switch (command) {
      case 0x00: // (NUL) Do nothing / ignore
        break;
      case 0x69: // Initialize eyes
        {
          if (acknowledge(checksum, Serial.read())) {
            initializeEyes();
          }
          break;
        }
      case 0x56: // Version information
        {
          if (acknowledge(checksum, Serial.read())) {
            sendVersionInfo();
          }
        }
        break;
      case 0x70: // Eye position
        {
          while(Serial.available() < 5);
          Serial.readBytes(bytes, 4); // Read in eye position
          checksum += bytes[0] + bytes[1] + bytes[2] + bytes[3]; // Calculate checksum
          if (acknowledge(checksum, Serial.read())) {
            xval = (bytes[0] << 8) | bytes[1]; // Set eye x
            yval = (bytes[2] << 8) | bytes[3]; // Set eye y
          }
          break;
        }
      case 0x6C: // Eyelid position
        {
          while(Serial.available() < 3);
          Serial.readBytes(bytes, 2); // Read in eyelid position
          checksum += bytes[0] + bytes[1]; // Calculate checksum
          if (acknowledge(checksum, Serial.read())) {
            trimval = (bytes[0] << 8) | bytes[1]; // Set eyelid position
          }
          break;
        }
      case 0x62: // Blink
        {
          while(Serial.available() < 2);
          uint8_t delay = Serial.read(); // Read in blink delay
          checksum += delay; // Calculate checksum
          if (acknowledge(checksum, Serial.read())) {
            blink(delay * 10); // Play blink animation
            Serial.write(READY);
          }
          break;
        }
      case 0x61: // Animation
        {
          // Serial1.println("Animation command");
          // Serial1.print("97, ");
          while(Serial.available() < 3);
          Serial.readBytes(bytes, 2); // Read in animation information
          checksum += bytes[0] + bytes[1]; // Calculate checksum
          // Serial1.print(bytes[0]);
          // Serial1.print(", ");
          // Serial1.print(bytes[1]);
          // Serial1.print(", ");
          uint16_t animation = (bytes[0] << 4) | (bytes[1] >> 4); // Get animation number
          uint8_t numBytes = bytes[1] & 0x0F; // Get number of animation arguments
          // Serial1.println(animation);
          // Serial1.println(numBytes);
          if (numBytes != 0) { // If animation arguments are expected
            while(Serial.available() < numBytes);
            Serial.readBytes(bytes, numBytes); // Read in animation arguments
            for (int i = 0; i < numBytes; i++) {
              checksum += bytes[i];  // Calculate checksum
              // Serial1.print(bytes[i]);
              // Serial1.print(", ");
            }
            // Serial1.println();
          }
          if (acknowledge(checksum, Serial.read())) {
            playAnimation(animation, bytes); // Play animation
            Serial.write(READY);
          }
          break;
        }
      default:
        {
          Serial1.print(F("Unknown command ("));
          Serial1.print(command);
          Serial1.println(F(")"));
          acknowledge(checksum, 0);
        }
    };
  }

  updateEyes();

  delay(5);
}

bool acknowledge(byte checksum1, byte checksum2) {
  // Serial1.println(checksum1);
  // Serial1.println(checksum2);
  if (checksum1 == checksum2) {
    Serial.write(ACK);
    // Serial1.println(F("ACK"));
    return true;
  } else {
    Serial.write(NAK);
    Serial1.println(F("NAK"));
    return false;
  }
}

void initializeEyes() {
  xval = 512;
  yval = 512;
  trimval = 500;
  switchval = 0;
}

void sendVersionInfo() {
  Serial.write(majorVersion);
  Serial.write(minorVersion);
  Serial.write(patchVersion);
  Serial.write(strlen(versionDate));
  Serial.write(versionDate);
  Serial.write(READY);
}

void updateEyes() {
  lexpulse = map(xval, 0, 1023, 220, 440);
  rexpulse = lexpulse;

  leypulse = map(yval, 0, 1023, 250, 500);
  reypulse = map(yval, 0, 1023, 400, 280);

  trimval2 = map(trimval, 320, 580, -40, 40);
  uplidpulse = map(yval, 0, 1023, 400, 280);
  uplidpulse -= (trimval2 - 40);
  uplidpulse = constrain(uplidpulse, 280, 400);
  altuplidpulse = 680 - uplidpulse;

  lolidpulse = map(yval, 0, 1023, 410, 280);
  lolidpulse += (trimval2 / 2);
  lolidpulse = constrain(lolidpulse, 280, 400);
  altlolidpulse = 680 - lolidpulse;

  pwm.setPWM(0, 0, lexpulse);
  pwm.setPWM(1, 0, leypulse);

  if (switchval == HIGH) {
    pwm.setPWM(2, 0, 400);
    pwm.setPWM(3, 0, 240);
    pwm.setPWM(4, 0, 240);
    pwm.setPWM(5, 0, 400);
  } else if (switchval == LOW) {
    pwm.setPWM(2, 0, uplidpulse);
    pwm.setPWM(3, 0, lolidpulse);
    pwm.setPWM(4, 0, altuplidpulse);
    pwm.setPWM(5, 0, altlolidpulse);
  }
}

void playAnimation(uint16_t animation, char* arguments) {
  switch (animation) {
    case 0: // Dummy animation
      break;
    case 1: // Blink (0x????) delay in ms
      blink((arguments[0] << 8) | arguments[1]);
      break;
    case 2:
      eyeroll((arguments[0] << 8) | arguments[1], (arguments[2] << 8) | arguments[3]);
      break;
    default:
      Serial1.print(F("Unknown animation ("));
      Serial1.print(animation);
      Serial1.println(F(")"));
  }
}

void blink(uint16_t delayTime) {
  switchval = 1;
  updateEyes();
  delay(delayTime);
  switchval = 0;
  updateEyes();
}

void eyeroll(uint16_t delayTime, uint8_t intensity) {

}

// Anamatronic Eyes Communication Protocol

// From PC to Arduino
// 0x00 (NUL) Do nothing / ignore
// 0x69 (i) Initialize eyes
//     * 0x??   Checksum
//     - Response: ACK
// 0x70 (p) Eye position
//     * 0x???? Eye X
//     * 0x???? Eye Y
//     * 0x??   Checksum
//     - Response: ACK
// 0x6C (l) Eyelid position
//     * 0x???? Eyelid
//     * 0x??   Checksum
//     - Response: ACK
// 0x62 (b) Blink
//     * 0x??   Blink delay
//     * 0x??   Checksum
//     - Response: ACK
//     - After animation: R
// 0x61 (a) Animation
//     * 0x???  Animation number
//     * 0x?    Number of animation argument bytes
//     * Animaion arguments
//     * 0x??   Checksum
//     - Response: ACK
//     - After animation: R

// From Arduino to PC
// 0x52 (R) Ready
// 0x06 (ACK) Acknowledge
// 0x15 (NAK) Negative Acknowledge
