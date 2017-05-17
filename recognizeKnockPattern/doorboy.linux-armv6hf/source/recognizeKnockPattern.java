import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 
import processing.io.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class recognizeKnockPattern extends PApplet {

//takes in mouse input as a knock
PImage bg;
//import processing.sound.*;
//SinOsc soundEffect;
//float volume=0;
//ArrayList<Ripple> rippleknocks = new ArrayList<Ripple>();//large visual effect circles
FloatList knockTimes;// records the actual millis times that the knock was recieved (cleared each time)
FloatList knockIntervals;// calculates from knockTimes the intervals in millis between each knock (cleared each timwe)
FloatList intervalRatios;

Serial arduinoPort;

SoftwareServo servo;

int totalknocking=0;//# of total knocks ever inputted
int howManyKnocks;
boolean open = false;
float openSince = 0;
float timeSince =0;//time since the last knock was put in, after a certain amount of time, it'll automatically assess the knock

JSONArray values;
int previousEventTime=0;

int serialcounter = 0;
public void setup(){
  bg = loadImage("door.jpg");
  knockTimes = new FloatList();
  knockIntervals = new FloatList();
  intervalRatios = new FloatList();
  
  values = loadJSONArray("passwords.json");
  //create the sine oscillator for sound effects
  //soundEffect = new SinOsc(this);
  //soundEffect.freq(100);
  //soundEffect.play();
  arduinoPort = new Serial(this, Serial.list()[0], 9600);
  servo = new SoftwareServo(this);
  servo.attach(18);
  servo.write(125);
}
public void draw(){
  background(bg);
  //audio();//creates knocking sound effect
  //visualEffects();//creates ripples and tally marks
  if(open && (millis() - openSince)> 2000){
    //Here have the lock close again!
    println("open since has exceeded time expected");
    closeTheDoor();
    
    reset();
  }
}
//void mousePressed(){//a mouse click is a knock
//  //A serial thing is the knock! 
//  knockJustHappened();
//}

public void serialEvent (Serial p) {
  serialcounter++;
  checkCount();
      //println(p.readString());
      
      //knockJustHappened();  
}
public void checkCount(){
  if (serialcounter ==3){
    serialcounter=0;
    knockJustHappened();
  }
}
public void keyPressed(){
  reset();
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void knockJustHappened(){
         println("Knock! "+millis()+ " "+totalknocking);

  totalknocking++;
  
  float currentTime = millis();//current time is registered and stored
  if(knockTimes.size()>=1){
    if(currentTime - knockTimes.get(knockTimes.size()-1)>2500){//WAIT TIME BETWEEN NEW RECOGNITION IS 3.5 seconds
      reset();
    }
  }
  knockTimes.append(currentTime);
  
  howManyKnocks = knockTimes.size();
  if(howManyKnocks>1){
    float earlierKnock = knockTimes.get(howManyKnocks-2);
    float laterKnock = knockTimes.get(howManyKnocks-1);
    float interval = laterKnock - earlierKnock;
    knockIntervals.append(interval);//intervals are calculated and put into floatlist
  }
    //calculate interval ratios
  if(howManyKnocks>2){
    float intervalRelation = knockIntervals.get(knockIntervals.size()-2)/knockIntervals.get(knockIntervals.size()-1);
    intervalRatios.append(intervalRelation);
    calculateRhythm();
  }
  addNewEffects();
}
public void calculateRhythm(){
  for(int i=0;i<values.size();i++){//GO THROUGH EVERY PASSWORD IN THE FILE
    JSONObject rhythm = values.getJSONObject(i);
    //boolean matching = false;
    JSONArray averageRatio = rhythm.getJSONArray("AverageIntervalRatio");
    //println("averageRatio"+averageRatio.get(i)+" size"+averageRatio.size());
    JSONArray standardDevRatio = rhythm.getJSONArray("StandardDeviationsOfIntervalRatios");
    
    if(averageRatio.size()==intervalRatios.size()){
      int k=0;
      while(k<intervalRatios.size()){
     //for(int k=1; k<intervalRatios.size();k++){//go through the recorded ratios to see if it matches
       float diff = abs(averageRatio.getFloat(k)-intervalRatios.get(k));
       if(diff<2*standardDevRatio.getFloat(k)){//if the ratio difference is less than one standard dev away from recorded average...
         k++;
       }else{println("this knock is a no no");reset();k=intervalRatios.size()+5;
       }
      }
      if(k==intervalRatios.size()){
        //open = true;
        openTheDoor();
      }
    }
  }
}
public void openTheDoor(){
  bg = loadImage("doorOpen.jpg");
  openSince = millis();
  open = true;
  servo.write(30);
}
public void closeTheDoor(){
    bg = loadImage("door.jpg");
    open=false;
    servo.write(150);
}
///////////////////////////////VISUAL/SOUND EFFECTS/////////////////////////////////////////////////////////////////////
public void addNewEffects(){//add new ripples and tallies to the array of both
  //Ripple anotherKnock = new Ripple(mouseX, mouseY);
  //rippleknocks.add(anotherKnock);
  //volume=1;
  int howManySoFar = knockTimes.size();
}
public void reset(){
  //clear the float lists for the next round
  knockTimes.clear(); knockIntervals.clear();//clears the current floatLists of knock times and intervals
  intervalRatios.clear();
  howManyKnocks=0;
}
//void visualEffects(){
//  //RIPPLE 
//  for (int i = 0; i < rippleknocks.size(); i++) {
//    if(rippleknocks.get(i).keep){//if the ripple is still viable to grow, continue drawing it out
//      rippleknocks.get(i).draw();
//      rippleknocks.get(i).update();
//    }else{
//      rippleknocks.remove(i);
//    }
//  }
//}
//void audio(){
//  soundEffect.pan(map(mouseX,0,width,-1,1));//x position determines what side the osund comes out from
//  soundEffect.freq(map(mouseY,0,height,300,80));//y position determines pitch
//  soundEffect.amp(volume);
//  volume=volume/1.2;
//}
class Ripple{
  float x;
  float y;
  int size=1;
  float increase = width/25; 
  int strokeC=255;
  boolean keep = true;
  Ripple(float xx, float yy){
    x=xx;
    y=yy;
  }
  public void update() {
    increase-=width/2222.22f;
    size+=increase;
    strokeC-=5;
    if (strokeC<=0){
      keep = false;
    }
  }
  public void draw(){
    stroke(255,strokeC);
    //fill(255,strokeC);
    noFill();
    ellipse(x,y,size,size);
  }
}
  public void settings() {  size(800,800); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "recognizeKnockPattern" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
