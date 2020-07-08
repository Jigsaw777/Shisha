package com.example.shareittest

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_sender.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.Socket
import java.net.URI
import java.net.URLDecoder

class SenderActivity : AppCompatActivity() {

    private val uriList= mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender)
        init()
    }

    private fun init(){
//        val actualThread=Thread(ClientThread())
//        actualThread.stop()
        send_to_button.setOnClickListener{
            if(uriList.isEmpty()){
                Toast.makeText(this, "No files selected to send", Toast.LENGTH_SHORT).show()
            }
            else {
                Thread(ClientThread(uriList)).start()
            }
        }

        select_file_button.setOnClickListener{
            selectFiles()
        }
    }

    private fun selectFiles() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "*/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        uriList.clear()
        if (data?.data == null) {
            if(data?.clipData != null){
                val uris=data.clipData as ClipData
                for(i in 0 until uris.itemCount)
//                    uriList.add(uris.getItemAt(i).uri.path?:"")
                    uriList.add(PathUtils.getPath(this,uris.getItemAt(i).uri)?:"")
            }
            else{
                Toast.makeText(this, "Please select some file", Toast.LENGTH_SHORT).show()
            }
        } else {
//            uriList.add(data.data?.path?:"")
            uriList.add(PathUtils.getPath(this, data.data?: Uri.EMPTY)?:"")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}


class ClientThread(private val list:List<String>):Runnable{
    override fun run() {
//        val s= Socket("192.168.43.8",9999)
        for(url in list){
            val s= Socket("192.168.43.8",9999)
            val decodedUrl=URLDecoder.decode(url,"UTF-8")
            val myFile=File(decodedUrl)
            val myByteArray = ByteArray(myFile.length().toInt())
            val fis=FileInputStream(myFile)
            val bis=BufferedInputStream(fis)
            bis.read(myByteArray,0,myByteArray.size)

            val os=s.getOutputStream()
            os.write(myByteArray,0,myByteArray.size)
            os.flush()
            s.close()
        }
//        s.close()
    }
}