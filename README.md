# mgw-java

## Overview

Sample java client application using [SWIFT Microgateway](https://developer.swift.com/swift-microgateway) to call SWIFT gpi Get Changed Payment Transaction API.

## Getting Started

### Prerequisites

* Java 1.8
* maven 3.5.* and above

### Install & Run packages

Modify ClientApp.java to match with MGW configuration (like Application Name)
```
mvn clean package assembly:single

java -jar target\mgwclient-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

## Authors

vijay.mukundhan@swift.com

## License

Apache v2.0

SWIFT is not liable for the usage of this sample app.
