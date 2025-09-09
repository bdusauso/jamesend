#!/bin/bash

echo "🚀 Starting ActiveMQ Artemis..."
docker-compose up -d

echo "⏳ Waiting for ActiveMQ Artemis to start..."
sleep 15

echo "🔍 Checking ActiveMQ Artemis status..."
docker-compose ps

echo ""
echo "📋 ActiveMQ Artemis is ready!"
echo ""
echo "🌐 Web Console: http://localhost:8161/console/"
echo "   Username: admin"
echo "   Password: admin"
echo ""
echo "🔌 JMS Connection URL: tcp://localhost:61616"
echo ""
echo "📨 Test destinations you can use:"
echo "   - Queue: test.queue"
echo "   - Topic: test.topic"
echo ""
echo "🎯 To run the JMS GUI application:"
echo "   ./run.sh"
echo ""
echo "🛑 To stop ActiveMQ Artemis:"
echo "   docker-compose down"