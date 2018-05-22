# Broadlink binding for OpenHab

## Created by Cato Sognen

https://community.openhab.org/t/broadlink-binding-for-rmx-a1-spx-and-mp-any-interest/22768/56

Under you can find the links to download a Broadlink binding together with instructions on how to get it working on your system. Supported devices in this version are:

•	RM and RM2 - IR and RF transmitter with temperature sensor and Wi-Fi connectivity
•	RM3 - IR transmitter with Wi-Fi connectivity
•	A1 - multi sensor that can detect temperature, humidity, illumination, background noise and air quality

In addition, it includes code to support three kinds of smart sockets (listed under). Since I don’t have any of these they are untested and may not work.

Drop me a message if you have any of these devices and are interested in working with me to get them working with the binding.

•	SP1 - smart socket with Wi-Fi connectivity
•	SP2 - as above with addition features
•	MP1 - four port power strip with Wi-Fi connectivity

Also, with this being the first version I do expect there will be issues. Please post whatever problems you are having and I will do my best to sort them out.
The binding can be downloaded here: (see later post for latest version) Once downloaded, place it in your openhab/addons folder.

# Step by Step
## Step 1: Inside openhab go to your Inbox and search for devices using the Broadlink binding. If the binding does not find your devices make sure that they are on the same network and are online.
## Step 2: For each found device click on the “Add” button to add it as a thing.
## Step 3: Go to your Thing screen under Configuration and click on the device you added above.
## Step 4: For each device you are required to enter a common Authentication Key and IV parameter. These were obtained and shared by someone who reversed engineered the protocol and such due to legal reason I cannot post these two items here. If you do not have these two then Google is your friend. Both need to be entered in hex string format by omitting the 0x. For example, if the key is something like 0x09, 0x76, 0x28… you will enter this as 097628… Once done, you should have two hex strings, each with a length of 32 characters.
## Step 5: For RMx devices only. The Map file refers to the file that holds the IR/RF codes. It defaults to broadlink.map but you can change it to something else if you prefer. Go to your openhab/transform folder and create a broadlink.map file using your favourite text editor.
## Step 6: For RMx devices only. In this step, we will find IR/RF code to insert into the broadlink.map file. Go to http://rm-bridge.fun2code.de/rm_manage/code_learning.html337 to learn the various IR/RF codes. After learning a function you should see something like this:

{“api_id”:1004,“command”:“send_code”,“mac”:“B4:43:0D:38:FA:9D”,“data”:“260068000001289314101312143513121411131213111411133613361510143614351336143614351411133615101411131213111313131014361412123712361436133615351435140005230001274815000c4d0001284a12000c4d0001284915000c4b00012a4814000d05”}
Insert the data string into the broadlink.map file together with the function. For example, I inserted the following line into broadlink.map for turning on my tv.

TV_POWER_ON = 260068000001289314101312143513121411131213111411133613361510143614351336143614351411133615101411131213111313131014361412123712361436133615351435140005230001274815000c4d0001284a12000c4d0001284915000c4b00012a4814000d05

Insert a new line for each of the function you want openhab to control.

## Step 7: For RMx devices only. Create an item in your items file and add it to your sitemap.

item file:
String TV_LIVINGROOM “TV” { channel=“broadlink:rm2:b4-43-0d-38-fa-9d:command” }

sitemap file:
Switch item=TV_LIVINGROOM label=“Power” mappings=[TV_POWER_ON=“On”, TV_POWER_OFF=“Off”]

Next Steps:
•	Resolve reported bugs
•	Get this thing on GitHub
•	Implement support for Broadlink Alarm Kit

