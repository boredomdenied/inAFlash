# inAFlash
Udacity Android NanoDegree Capstone Project


## Problem:

Several years back now I took an Uber and started chatting with the driver. I asked how he liked the job and he said it was overall good; however sometimes things can get chaotic. I asked if he wouldn’t mind elaborating and was told of a situation where his fare would be at a large event. Arriving to a crowd of participants looking for their Uber/Lyft, it was confusing to match fare with ride. Instantaneously I solved this problem.


## Solution:

I saw a scene with driver and rider phones flashing the same color and number. This was a link which could provide both parties the means to verify each other. The rider will orient their displays so that the screen is facing out towards the rides streaming by after the “your driver is arriving” is displayed. From the drivers POV, they see a card with the same pattern within their app and can view the match outside amongst the crowd. Two way verification can be had by driver rolling down the window and showing it back to the rider.

## Testing

Please note in order to test this app you will need to grab a Google Maps API key. Insert this into the AndroidManifest.xml file where it currently shows YOUR_API_KEY_HERE. 
You will also need a firebase project. Once you have gone through the process of setting up a firebase project for Android apps, put your google-services.json file the app root directory. The data structure needed within your realtime database is as follows:

## flash =>
  color,
  number
## provider =>
  connected,
  latitude,
  longitude
## requestor =>
  connected,
  latitude,
  longitude

This requires two devices and GPS spoofing. You will need to differentiate coordinates so that the distance between requestor and provider is greater than 100 meters. After this has been set you can set the role on both devices, and now proceed to set the GPS so that they are very close or identical (within 100m). 

## Development 

The current state of this app is POC/Demo status. It will be enhanced to include more functionality. The current process kicks off an activity when the distance is less than 100m. The idea going forward is to modularize this, so either a Java or Android library will be had. Also I want to add features/options so that developers can customize the function. Ideas include different sizes for the display, behavior on click, multipe digits, adding more colors. That's a decent start. If you would like to see additional features please feel free to submit a PR. 
