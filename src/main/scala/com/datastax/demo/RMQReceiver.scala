/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datastax.demo

import akka.actor.{ActorPath, ActorLogging, Actor, ActorRef}
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.{Channel, ConnectionFactory, DefaultConsumer, Envelope}
import com.thenewmotion.akka.rabbitmq._
import org.apache.spark.streaming.receiver._

import scala.concurrent.duration.DurationInt

/**
 * A Spark Receiver that subscribes to a RabbitMQ stream.
 */
class RMQReceiver(host : String,
                  username : Option[String],
                  password : Option[String],
                  qname : String,
                  exchange: String)
  extends Actor with ActorHelper {

  implicit val timeout = 5 seconds

  val factory = new ConnectionFactory()
  username.map(factory.setUsername(_))
  password.map(factory.setPassword(_))
  factory.setHost(host)

  override def preStart() = {

    log.debug("Starting Consumer Actor")

    val connectionActor: ActorRef = context.actorOf(ConnectionActor.props(factory))

    def setupChannel(channel: Channel, self: ActorRef) {

      channel.queueBind(qname,exchange,"")
      val consumer = new DefaultConsumer(channel) {

        override def handleDelivery(consumerTag: String,
                                    envelope: Envelope,
                                    properties: BasicProperties,
                                    body: Array[Byte]): Unit = {

          //Minor hack placing args in a sequence because slf4j has an ambiguous vararg .debug(..) call
          log.trace("Message received: {} - {}", Seq(fromBytes(body), properties.getMessageId):_*)
          store(fromBytes(body))

        }
      }
      //Start the consumer with auto-acknowledge on
      channel.basicConsume(qname, true, consumer)
    }
    val channelActor: ActorRef = connectionActor.createChannel(ChannelActor.props(setupChannel))

  }

  def fromBytes(msg: Array[Byte]) : String = new String(msg, "UTF-8")

  def receive: Receive = {
    case unknownMsg => logInfo("Actor received $unknownMsg message but was not expecting it...")
  }


}