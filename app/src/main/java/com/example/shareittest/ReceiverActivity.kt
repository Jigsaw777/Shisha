package com.example.shareittest

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_receiver.*
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket

class ReceiverActivity : AppCompatActivity() {

    private lateinit var serverSocket: ServerSocket
    private lateinit var serverThread: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver)
        init()
    }

    private fun init() {
        serverSocket = ServerSocket()
        serverSocket.reuseAddress = true
        serverSocket.bind(InetSocketAddress(9999))
        serverThread = ServerThread(serverSocket, this)
        val actualThread = Thread(serverThread)
        receive_file.setOnClickListener {
            if (!actualThread.isAlive)
                actualThread.start()
        }
    }
}

class ServerThread(private val serverSocket: ServerSocket, private val context: Context) :
    Runnable {
    override fun run() {
        while (!Thread.currentThread().isInterrupted) {
            val socket = serverSocket.accept()
            val startTime = System.currentTimeMillis()
            val fileName =
                File(Environment.getExternalStorageDirectory().path + "/ShareItTestFiles")
            if (!fileName.exists() && !fileName.isDirectory)
                fileName.mkdirs()
            val inputStream = socket.getInputStream()
            var tempName = ""
            val bis = BufferedInputStream(inputStream)
            bis.mark(2)
            val nameLength = bis.read()
            Log.d("receiver side", "Length of file name : $nameLength")
            val fileSize=bis.read()
            Log.d("receiver side", "Size of file : $fileSize")
            bis.reset()
            bis.mark(nameLength + 2)
            bis.read()
            bis.read()
            for (i in 0 until nameLength)
                tempName += bis.read().toChar().toString()
            Log.d("act","File name : $tempName")
            bis.reset()
            val outputStream = FileOutputStream(File(fileName, tempName))
            for(i in 0 until (nameLength+2))
                bis.read()
            IOUtils.copy(bis,outputStream)

            inputStream.close()
            socket.close()
            Log.d("act", "Transfer done in ${(System.currentTimeMillis() - startTime) / 1000}")
        }
    }
}