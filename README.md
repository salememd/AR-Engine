## Resources Sharing Platform Using Augmented Reality
This project aims to develop resources sharing platform that allows users to share any type of files with each other. The platform provides a set of characteristics that improve the way of how humans interact with computers.

The resources sharing platform enables users to perform an image-based search to find resources published through the platform and provides the ability to view digital information such as images, audios and videos using augmented reality technology in real-time using a smartphone application.

The system itself divided into distributed sub-systems that communicate with each other to perform multiple tasks, this approach helps to achieve efficiency and increase the performance speed.

<p align="center"> 
<img  height="350"  src="https://raw.githubusercontent.com/salememd/AR-Engine/master/doc/11.png">
</p>
<p align="center">System architecture<p align="center">

Upload Content To Platform Video
<p> 
<a href="https://www.youtube.com/watch?v=Ms3auGDOU2I"><img  height="222" src="https://img.youtube.com/vi/Ms3auGDOU2I/0.jpg"></a>
</p>

AR Experience Video
<p> 
<a href="https://www.youtube.com/watch?v=FgQzLKTuAzY"><img  height="222" src="https://img.youtube.com/vi/FgQzLKTuAzY/0.jpg"></a>
</p>

Image Search Experience Video
<p> 
<a href="https://www.youtube.com/watch?v=BC8rKTmcViw"><img  height="222" src="https://img.youtube.com/vi/BC8rKTmcViw/0.jpg"></a>
</p>

## 1) AR Engine
The AR engine is a separate software that performs an image-based search by matching a query image sent from the client application with the images published by the users through the platform and return a resource if a match occurred.

<p align="center"> 
<img src="https://raw.githubusercontent.com/salememd/AR-Engine/master/doc/1.png">
</p>
<p align="center">AR Engine data flow diagram in level 0<p align="center">

The published resources through the platform consist of the following files: -

 1. **Template image:** is the image that gets matched with the query image in the search process.
 2. **Preview file:** could be an image, audio or video that is returned to the client to be viewed if the published resource supports augmented reality experience.
 3. **Resource file:** the actual file that the client seeks.
## 1.1) Image Based Search

The AR engine uses ORB (Oriented FAST and Rotated BRIEF) algorithm and performs the matching process between keypoints to find a resource published by a user through the platform. The process begins when the client application sends a query image to the AR engine, the AR engine receives the image and prepare it for the search process by detecting the keypoints and calculating their descriptors, the search process takes the descriptors of the query image and match it with pre-calculated descriptors of the recourse’s template image that has been stored in the memory, if a match occurred, the resource file and preview file will be sent to the client application along with metadata used for handling the preview file [10].
<p align="center"> 
<img  height="350"  src="https://raw.githubusercontent.com/salememd/AR-Engine/master/doc/2.png">
</p>
<p align="center">AR Engine data flow diagram in level 1<p align="center">

It’s necessary to take the following points into consideration during the implementation process of the AR engine: -

 1. Programming language: the AR engine must be developed using a compiled language such as Java or C++ to achieve good performance.
 2. Search speed: it’s important to avoid any unnecessary operation in the search process to get results as quickly as possible.
 3. Search accuracy: the search process must return accurate results as much as possible.

## 1.2) System Design
## 1.2.1) Communication Protocol
AR engine uses an application-level  protocol to achieve real-time communication between the client application and the AR engine.
<p align="center"> 
<img  height="200"  src="https://raw.githubusercontent.com/salememd/AR-Engine/master/doc/3.png">
</p>
<p align="center">Communication between smartphone and AR engine<p align="center">

The session begins when a client initiates a request by establishing a Transmission Control Protocol (TCP) connection to port 1232 on the AR server. After the connection successfully established, the client application will begin sending the camera frames to be processed on the server and waits for a response. Upon receiving the response, the client will handle the response message and send another frame to be processed.

<p align="center"> 
<img  height="300"  src="https://raw.githubusercontent.com/salememd/AR-Engine/master/doc/4.png">
</p>
<p align="center">Communication sequence<p align="center">

The connection remains open until one of the pairs disconnect.

## 1.2.1.1) Messages Specification
<p align="center">Request message specification<p align="center">
<p align="center"> 
<img  height="60"  src="https://raw.githubusercontent.com/salememd/AR-Engine/master/doc/7.PNG">
</p>


<p align="center">Response message specification<p align="center">
<p align="center"> 
<img  height="360"  src="https://raw.githubusercontent.com/salememd/AR-Engine/master/doc/8.PNG">
</p>



<p align="center"> 
<img  height="360"  src="https://raw.githubusercontent.com/salememd/AR-Engine/master/doc/9.png">
</p>
<p align="center">AR engine connection states<p align="center">

## 1.2.2) Class Diagram

<p align="center"> 
<img   src="https://raw.githubusercontent.com/salememd/AR-Engine/master/doc/5.png">
</p>
<p align="center">AR engine class diagram<p align="center">

## 1.3) Behavioral Design
## 1.3.1) Image Search Functionality
<p align="center"> 
<img   src="https://raw.githubusercontent.com/salememd/AR-Engine/master/doc/6.png">
</p>
<p align="center">Image Search sequence diagram<p align="center">

## 1.4) Testing
The AR engine will be tested by filling its storage with a dataset consisting of 1000 unique images that will be used in the search process, each image in the dataset will be associated with a unique value representing the resource ID, a set of random images will be taken from the test images to be used as a query images and test whether the AR engine will be able to return the resource ID associated with them or not, the test cases will focus on the performance and accuracy of the AR engine.

## 1.4.1) Test Environment
The test process will be executed on a laptop computer with the following specifications: -
 - Processor: Intel CORE i7 6700 2.60GHz.
 - RAM: 16GB.
 - OS: Windows 10 64-bit
## 1.4.2) Test Results
<p align="center">AR engine test cases<p align="center">
<p align="center"> 
<img src="https://raw.githubusercontent.com/salememd/AR-Engine/master/doc/10.PNG">
</p>


Number of successful test cases: 21

Number of failed test cases: 4

Average execution time: 1.291 seconds.

Some of the test cases were failed when using a query image contains only text due to the inability to recognize characters in the feature matching approach that is used in the search process.
