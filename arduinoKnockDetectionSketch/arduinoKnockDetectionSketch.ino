
const int sampleWindow = 10; // Sample window width in mS (50 mS = 20Hz)
unsigned int sample;
 bool knock = false; 
int numknock=0;
 
void setup() 
{
   Serial.begin(9600);

}
 
 
void loop() 
{
   unsigned long startMillis= millis();  // Start of sample window
   unsigned int peakToPeak = 0;   // peak-to-peak level
 
   unsigned int signalMax = 0;
   unsigned int signalMin = 1024;
 
   // collect data for 50 mS
   while (millis() - startMillis < sampleWindow)
   {
      sample = analogRead(0);
      if (sample < 1024)  // toss out spurious readings
      {
         if (sample > signalMax)
         {
            signalMax = sample;  // save just the max levels
         }
         else if (sample < signalMin)
         {
            signalMin = sample;  // save just the min levels
         }
      }
   }
   peakToPeak = signalMax - signalMin;  // max - min = peak-peak amplitude
   double volts = (peakToPeak * 5.0) / 1024;  // convert to volts

   if (volts < 1.2)
   {

    knock = false; 
   }
   
   if (volts > 3 && knock == false)
   {
    knock = true;
    numknock++;
    Serial.println("a");
//    Serial.println(numknock);
   }


   
//   Serial.println(volts);
}
