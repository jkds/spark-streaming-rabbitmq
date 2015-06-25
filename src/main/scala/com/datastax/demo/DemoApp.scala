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

import java.util.UUID

import akka.actor._
import com.datastax.spark.connector.SomeColumns
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.streaming._
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.concurrent.duration._

/**
 * Demo application that streams messages from RabbitMQ and allows them to be consumed
 * from Spark
 */
object DemoApp extends App {

  implicit val system = ActorSystem("data-feeder")
  implicit val timeout = 10 seconds

  val settings = new DemoSettings()

  import settings._

  val log = system.log

  lazy val conf = new SparkConf().setAppName("rmq-receiver-demo")
    .setMaster(SparkMaster)
    .set("spark.executor.memory", SparkExecutorMemory)
    .set("spark.default.parallelism", SparkParallelism)
    .set("spark.cassandra.connection.host", CassandraHosts)
    .set("spark.cassandra.auth.username", "cassandra")
    .set("spark.cassandra.auth.password", "cassandra")

  log.info("Lazily creating spark context")
  lazy val sc = new SparkContext(conf)
  //Create a keyspace and table for placing the messages in
  createSchema(conf)

  lazy val ssc = new StreamingContext(sc, Milliseconds(SparkStreamingBatchWindow))
  //Create an RMQReciver actor stream that is used to publish items of type String
  val msgs = ssc.actorStream[String](Props(classOf[RMQReceiver], RMQHost,
    Some(RMQUsername),
    Some(RMQPassword),
    RMQQueuename,
    RMQExchange), "rmq-receiver")
  //Dump Messages to a log table with a unique id....
  msgs.map { msg =>
    (UUID.randomUUID(), msg)
  } saveToCassandra("msgs", "msg_audit", SomeColumns("uid", "msgbody"))

  //Start all streams...
  ssc.start()
  //Shutdown Hook
  system.registerOnTermination {
    ssc.stop(stopSparkContext = true, stopGracefully = true)
  }

  log.info("Awaiting termination...")

  ssc.awaitTermination()
  system.awaitTermination()

  def createSchema(conf: SparkConf): Boolean = {
    CassandraConnector(conf).withSessionDo { sess =>
      sess.execute("CREATE KEYSPACE IF NOT EXISTS msgs WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
      sess.execute("CREATE TABLE IF NOT EXISTS msgs.msg_audit (uid uuid primary key, msgbody text)")
    } wasApplied
  }


}

