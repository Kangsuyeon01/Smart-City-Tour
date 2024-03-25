package com.aiproject.landmarkapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

class ImgActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_img)

        val btnGoToMain = findViewById<View>(R.id.btn_gotomain) as Button
        val btnMaps = findViewById<View>(R.id.btn_maps) as Button
        val imagePreview = findViewById<View>(R.id.imagePreview) as ImageView
        val txtLandMarkName = findViewById<View>(R.id.txtName) as TextView
        val txtLandmarkDescription= findViewById<View>(R.id.txtDescription) as TextView

        val imgPath= intent.getStringExtra("image")
        val landmarkName = intent.getStringExtra("landmarkName")
        val landmarkDescription = intent.getStringExtra("landmarkDescription")

        val photoUri: Uri? = imgPath?.toUri()



        imagePreview.setImageURI(photoUri)
        txtLandMarkName.text = landmarkName
        txtLandmarkDescription.text = landmarkDescription
        txtLandmarkDescription.movementMethod = ScrollingMovementMethod()



        btnGoToMain.setOnClickListener{
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)

        }

        btnMaps.setOnClickListener{
            val naverMapsIntent = Intent(applicationContext, NaverMapsActivity::class.java)
            startActivity(naverMapsIntent)

        }


    }



}