package com.example.shareittest

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.util.Log
import kotlinx.android.synthetic.main.activity_receiver.*
import org.apache.commons.io.IOUtils
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class ReceiverActivity : AppCompatActivity() {

    companion object{
        var fileSequence=1
    }

    private lateinit var serverSocket:ServerSocket
    private lateinit var serverThread:Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver)
        init()
    }

    private fun init(){
        serverSocket = ServerSocket()
        serverSocket.reuseAddress=true
        serverSocket.bind(InetSocketAddress(9999))
        serverThread=ServerThread(serverSocket,this)
        val actualThread=Thread(serverThread)
        receive_file.setOnClickListener{
            if(!actualThread.isAlive)
               actualThread.start()
        }
    }
}

class ServerThread(private val serverSocket: ServerSocket, private val context: Context):Runnable{
    override fun run() {
        while(!Thread.currentThread().isInterrupted) {
            val socket = serverSocket.accept()
            val startTime=System.currentTimeMillis()
            val fileName= File(Environment.getExternalStorageDirectory().path+"/ShareItTestFiles")
            if(!fileName.exists() && !fileName.isDirectory){
                fileName.mkdirs()
//                fileName.createNewFile()
            }
            val inputStream=socket.getInputStream()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                Files.copy(inputStream,fileName.toPath()+Paths.get("hello_first"),StandardCopyOption.REPLACE_EXISTING)
                Files.copy(inputStream,File(fileName.toPath().toString(),"hello_first_${ReceiverActivity.fileSequence++}.mp4").toPath(),StandardCopyOption.REPLACE_EXISTING)
            }
            else{
                val outputStream=FileOutputStream(File(fileName,"hello_first_${ReceiverActivity.fileSequence++}.mp4"))
                IOUtils.copy(inputStream,outputStream)
            }

            inputStream.close()
            socket.close()
            Log.d("act","Transfer done in ${(System.currentTimeMillis()-startTime)/1000}")
        }
    }
}