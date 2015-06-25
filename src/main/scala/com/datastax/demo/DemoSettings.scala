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

import com.typesafe.config.{ConfigFactory, Config}

/**
 * Settings for this demo, read from application.conf
 */
class DemoSettings(conf : Option[Config] = None) {

  val rootConfig = ConfigFactory.load

  protected val cassandra                   = rootConfig.getConfig("cassandra")
  protected val spark                       = rootConfig.getConfig("spark")
  protected val rabbitmq                    = rootConfig.getConfig("rabbitmq")

  /* Cassandra Settings */
  val CassandraHosts                        = cassandra.getString("connection.host")
  /* Spark Settings */
  val SparkMaster                           = spark.getString("master")
  val SparkExecutorMemory                   = spark.getString("executor.memory")
  val SparkParallelism                      = spark.getString("parallelism")
  val SparkCleanerTTL                       = spark.getString("cleaner.ttl.seconds")
  val SparkStreamingBatchWindow             = spark.getInt("streaming.batch.interval")
  /* Rabbit MQ Settings */
  val RMQHost                               = rabbitmq.getString("host")
  val RMQUsername                           = rabbitmq.getString("username")
  val RMQPassword                           = rabbitmq.getString("password")
  val RMQQueuename                          = rabbitmq.getString("queuename")
  val RMQExchange                           = rabbitmq.getString("exchange")

}
