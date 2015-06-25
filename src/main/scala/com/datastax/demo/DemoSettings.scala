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
