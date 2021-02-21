# RADQ
  RADQ (Robô Autônomo Detector de Quedas - Fall Detector Autonomous Robot) is an Android application that has integration with an Arduino Robot, with the ability to follow an user by its object recognition capabilty and alert contacts if the user falls and requires assistance.

## Background
  An autonomous robot has the goal to perform many objectives and desired tasks in different environments without human intervention. They use to be used in many different areas such as logistics, production lines, or in the health field, for example.

  When it comes to the health field, the autonomous robot can be used with the goal of monitoring patients, mainly if they are elderly or have any kind of disability. To ensure the patients' safety, it is essential for the development of a monitoring system for this group in particular.

  This project presents the development of an autonomous system, making use of an Arduino controller and a smartphone to monitor the elderly and/or people with disabilities to detect probable falls streamlining the service that those people might need through emergency contact notifications.

  Systems' tests were made on flat environments, both wide opened and closed, and got a good result considering both autonomous mobility, and fall detection.

## Tech

RADQ uses the following open-source projects to work:
* [UsbSerial](https://github.com/felHR85/UsbSerial) - responsible for communications between an Android phone and an Arduino board
* [Darknet](https://github.com/AlexeyAB/darknet) - neural network framework used to perform object recognition
* [Android-Deep-Learning-with-OpenCV](https://github.com/ivangrov/Android-Deep-Learning-with-OpenCV) - special thanks for this particular repository that helped us on making the recognition using Yolo in Android happen
* [LabelImg](https://github.com/tzutalin/labelImg) - a graphical image annotation tool used to mark the images with our desired objects in order to deliver it to the Darknet framework
* [Fallen People Detection Capabilities Using Assistive Robot](https://www.mdpi.com/2079-9292/8/9/915/htm) - special thanks to this paper which we were able to use the FPDS dataset in order to train fallen people using the YOLO v3 model.

Also used:
* [Firebase](https://firebase.google.com/) - responsible for user authentication, alerts storage and notification delivery to contacts
* [Java](https://www.java.com/) - the application was built using Java Language
* [Android Studio](https://developer.android.com/studio) - the entire development were made using the Android Studio IDE and also the initial application tests
* [Google Colaboratory](https://colab.research.google.com/) - a computational solution used to execute the Darknet framework
* [Roboflow](https://roboflow.com/) - a web service created to augment the dataset of images used to train the neural network 

Arts by:
* [Pixel perfect](https://www.flaticon.com/authors/pixel-perfect)
* [itim2101](https://www.flaticon.com/authors/itim2101)
* [Freepik](https://www.flaticon.com/authors/freepik)

## Idealized by
* [André Bittencourt](https://github.com/DeBittencourt) (DeBittencourt)
* [Davi Merotto](https://github.com/DaviMerotto) (DaviMerotto)
* [Evelyn Silva](https://github.com/EveMari) (EveMari)
* [Nicolas Suzuki](https://github.com/nicolas-suzuki) (nicolas-suzuki)

License
----

MIT
