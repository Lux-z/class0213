package com.atguigu.bigdata.spark.streaming.test

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * @author lux_zhang
 * @create 2020-06-14 18:48
 */
object SparkStreamingDIY {
    def main(args: Array[String]): Unit = {
        val sparkConf = new SparkConf().setMaster("local[*]").setAppName("streaming")
        val ssc = new StreamingContext(sparkConf, Seconds(3))

        //执行逻辑  自己采集数据receiverStrea()
        val ds: ReceiverInputDStream[String] = ssc.receiverStream(new myReceiver("localhost",9999))
        ds.print()

        ssc.start()
        ssc.awaitTermination()
    }
    // 自定义数据采集器
    // 继承Reciver，定义泛型
    // Receiver的构造方法有参数的，所以子类在继承时，应该传递这个参数
    class myReceiver(host:String,port:Int) extends Receiver[String](StorageLevel.MEMORY_ONLY) {
        private var socket: Socket = _

        def receive():Unit = {
            val reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream, "UTF-8")
            )

            var s:String = null

            while (true) {
                s = reader.readLine()
                if (s != null) {
                    //将获取的数据保存到框架内部进行封装
                    store(s)
                }
            }
        }

        override def onStart(): Unit = {
            socket = new Socket(host, port)
            new Thread("Socket Receiver") {
                setDaemon(true)
                override def run() { receive() }
            }.start()
        }

        override def onStop(): Unit = {
            socket.close()
            socket = null
        }
    }
}
