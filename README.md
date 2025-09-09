# JMS GUI Sender

A JavaFX desktop application for sending JMS messages to topics and queues.

## Features

- **Server Connection**: Connect to JMS brokers using configurable server addresses
- **Authentication Support**: Store and use username/password credentials securely
- **Configuration Persistence**: Automatically saves and loads connection settings
- **Destination Support**: Send messages to both JMS topics and queues
- **JSON Payload**: Text area optimized for JSON message payloads
- **Real-time Logging**: Built-in log area showing connection status and message sending results
- **Cross-platform**: Runs on any platform with Java 21+

## Requirements

- Java 21 or higher
- JMS broker (e.g., Apache ActiveMQ Artemis)

## Building

```bash
mvn clean package
```

This will create an executable JAR file: `target/jms-gui-sender-1.0.0.jar`

## Running

### Using the JAR directly:
```bash
java --add-modules javafx.controls,javafx.fxml --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --enable-native-access=javafx.graphics -jar target/jms-gui-sender-1.0.0.jar
```

### Using the provided script:
```bash
./run.sh
```

### Using Maven:
```bash
mvn javafx:run
```

## Usage

1. **Server Connection**: 
   - Enter the JMS broker URL (e.g., `tcp://localhost:61616`)
   - Provide username/password if required (optional for anonymous brokers)
2. **Destination**: Choose between Topic or Queue and enter the destination name
3. **Payload**: Enter your JSON message payload in the text area
4. **Configuration**: Click "Save Config" to store your settings for next session
5. **Send**: Click "Send Message" to transmit the message
6. **Monitor**: View logs in the bottom panel for status updates

## Configuration Storage

The application automatically stores your settings in `~/.jms-gui-sender/config.json`:
- Server URL and credentials (password is Base64 encoded)
- Last used destination and type (topic/queue)
- Settings are loaded on startup and saved on exit

## Quick Start with Docker

### 1. Start ActiveMQ Artemis:
```bash
./test-setup.sh
```

This will:
- Start ActiveMQ Artemis in Docker
- Expose the web console at http://localhost:8161/console/ (admin/admin)
- Make JMS available at `tcp://localhost:61616`

### 2. Run the GUI application:
```bash
./run.sh
```

### 3. Test configuration:
- **Server**: `tcp://localhost:61616` (already pre-filled)
- **Username**: `admin` (for ActiveMQ Artemis)
- **Password**: `admin` (for ActiveMQ Artemis)
- **Destination**: Try `test.queue` or `test.topic`
- **Payload**: Use the provided JSON template
- **Save Config**: Click to persist your settings

### 4. Stop the test environment:
```bash
docker-compose down
```

## Default Configuration

- **Server**: `tcp://localhost:61616` (ActiveMQ Artemis default)
- **Destination Type**: Queue (pre-selected)
- **Sample Payload**: JSON template provided

## Supported JMS Brokers

The application uses Apache ActiveMQ 6.x client but should work with any JMS-compliant broker:

- Apache ActiveMQ Classic
- Apache ActiveMQ Artemis
- IBM MQ
- RabbitMQ (with JMS plugin)
- And other JMS 3.1 compliant brokers

## Architecture

- **JavaFX**: Modern desktop UI framework
- **Jakarta JMS 3.1**: Standard JMS API
- **Apache ActiveMQ 6.x**: JMS client implementation
- **Jackson**: JSON processing
- **Maven Shade Plugin**: Creates self-contained executable JAR

## Project Structure

```
src/main/java/com/example/jmsguisender/
├── JMSGuiSenderApplication.java  # Main application entry point
├── JMSGuiController.java         # GUI controller and event handling
├── JMSSender.java               # JMS messaging logic
├── ServerConfiguration.java     # Configuration data model
└── ConfigurationManager.java    # Configuration persistence
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

**Copyright 2025 Bruno Dusausoy <bruno.dusausoy@gmail.com>**

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.