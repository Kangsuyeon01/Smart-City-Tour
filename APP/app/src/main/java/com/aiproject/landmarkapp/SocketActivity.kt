package com.aiproject.landmarkapp

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.aiproject.landmarkapp.databinding.ActivitySocketBinding
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_socket.*
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.*


class SocketActivity : AppCompatActivity() {

    var photoUri: Uri? = null
    var imgpath = ""
    private val fbDatabase = Firebase.database
    private val labelRef = fbDatabase.reference.child("results")
    private val fbStore = Firebase.firestore
    private val resultRef = fbStore.collection("results")


    val timestamp = System.currentTimeMillis()
    var label = ""
    var landmarkName = ""
    var landmarkDescription = ""

    lateinit var storage: FirebaseStorage

    // Socket
    private var selected = false

    // Socket
    private var socket: Socket? = null
    private var outstream: DataOutputStream? = null
    private var instream: DataInputStream? = null
    private val port = 9999

    lateinit var cameraPermission: ActivityResultLauncher<String>
    lateinit var storagePermission: ActivityResultLauncher<String>



    lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    lateinit var galleryLauncher: ActivityResultLauncher<String>

    private val binding by lazy { ActivitySocketBinding.inflate(layoutInflater) }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var Camera_start  = intent.getStringExtra("Camera_start")
        var Gallery_start = intent.getStringExtra("Gallery_start")
        Log.d("camera",Camera_start.toString())

        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (cm.isDefaultNetworkActive) {
            Log.w("network", "Network Connected")
        } else {
            // 인터넷 연결 없음
        }
        binding.progressView.visibility = View.INVISIBLE
        binding.btnOkay.visibility = View.INVISIBLE
        storage = Firebase.storage

        storagePermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    setViews()
                } else {
                    Toast.makeText(baseContext, "You must approve external storage privileges before you can use the app.", Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
            }

        cameraPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    openCamera()
                } else {
                    Toast.makeText(baseContext, "You must approve camera permissions before you can use the camera.", Toast.LENGTH_LONG)
                        .show()
                }
            }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                if (isSuccess) {
                    binding.imagePreview.setImageURI(photoUri)
                    imgpath = photoUri.toString()
                }

            }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            binding.imagePreview.setImageURI(uri)
            imgpath = uri.toString()
            println(imgpath)
        }

        storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (Camera_start.toBoolean() == true){
            cameraPermission.launch(Manifest.permission.CAMERA)
            nextIntent()
        }
        else if (Gallery_start.toBoolean() == true){
            openGallery()
            nextIntent()
        }

    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun setViews() {
        binding.buttonCamera.setOnClickListener {
            cameraPermission.launch(Manifest.permission.CAMERA)
            nextIntent()
        }
        binding.buttonGallery.setOnClickListener {
            openGallery()
            nextIntent()
        }
    }

    fun openCamera() {
        val photoFile = File.createTempFile(
            "IMG_",
            ".jpg",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )
        cameraLauncher.launch(photoUri)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    //Firebase Storage에 이미지를 업로드 하는 함수.
    @RequiresApi(Build.VERSION_CODES.N)
    fun uploadImageTOFirebase(uri: Uri) {
        binding.progressView.visibility = View.VISIBLE
        val storage: FirebaseStorage = FirebaseStorage.getInstance()   //FirebaseStorage 인스턴스 생성
        //파일 이름 생성.
        val fileName = "${timestamp}.jpg"
        //파일 업로드, 다운로드, 삭제, 메타데이터 가져오기 또는 업데이트를 하기 위해 참조를 생성.
        //참조는 클라우드 파일을 가리키는 포인터라고 할 수 있음.
        Log.w("img", "" + fileName)
        val imagesRef =
            storage.reference.child("images/").child(fileName)    //기본 참조 위치/images/${fileName}
        Log.w("imgRef", "" + imagesRef)

        //이미지 파일 업로드
        val uploadTask = imagesRef.putFile(uri)

        uploadTask.addOnFailureListener {
            println("********************************업로드 실패**********************************")
            binding.progressView.visibility = View.INVISIBLE
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()

            // Handle unsuccessful uploads
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            println("********************************업로드 성공**********************************")
            binding.progressView.visibility = View.INVISIBLE
            socketFun()
            Toast.makeText(this, "Uploaded successfully!", Toast.LENGTH_SHORT).show()

        }
        uploadTask.addOnProgressListener { (bytesTransferred, totalByteCount) ->
            val progress = (100.0 * bytesTransferred) / totalByteCount
            Log.d(TAG, "Upload is $progress% done")
        }.addOnPausedListener {
            Log.d(TAG, "Upload is paused")
        }
    }

    private fun socketFun() {

        var indata: String = ""
        var outdata: String = ""
        selected = true

        // 파이썬 소켓 통신
        Log.w("connect", "연결 중..")
        val checkUpdate: Thread = object : Thread() {
            override fun run() {
                // Get ip
                val ip = "192.168.0.49"
                // Unix time (Specify User and Image)
                val id = timestamp.toString()
                // Connect to Server
                try {
                    socket = Socket(ip, port)
                    Log.w("Connected", "Successfully connected to server")
                } catch (e1: IOException) {
                    Log.w("Disconnected", "Can't connect to server")
                    e1.printStackTrace()
                }
                Log.w("value", "Android to Server")
                try {
                    outstream = DataOutputStream(socket!!.getOutputStream())
                    instream = DataInputStream(socket!!.getInputStream())
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.w("buffer", "error create buffer")
                }
                Log.w("buffer", "create buffer success")
                try {

                    while (true) {
                        if (selected) {
                            var data = id.toByteArray()
                            val b1 = ByteBuffer.allocate(data.size)
                            b1.order(ByteOrder.LITTLE_ENDIAN)
                            b1.putInt(data.size)
                            outstream!!.write(b1.array(), 0, data.size)
                            outstream!!.write(data)
                            outdata = String(data)
                            Log.w("out", "" + outdata)

                            data = ByteArray(data.size)
                            instream!!.read(data, 0, data.size)
                            val b2 = ByteBuffer.wrap(data)
                            b2.order(ByteOrder.LITTLE_ENDIAN)
                            val length = b2.int
                            instream!!.read(data, 0, length)
                            indata = String(data)

                            Log.w("in", "" + indata)

                            if (outdata == indata) {
                                Log.w("istrue", "True")

                                CoroutineScope(Dispatchers.IO).launch {
                                    getLabel(outdata)
                                    getResult(label)
                                }
                            }
                            selected = false
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        checkUpdate.start()

    }

    // 비동기 → 동기
    suspend fun getLabel(id: String) =
        suspendCoroutine<String> { continuation ->
            labelRef.child(id).child("result")
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        label = snapshot.getValue<String>().toString()
                        Log.w("saved label", label)
                        continuation.resume(label)
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

    fun getResult(l: String) {
        resultRef
            .document(l)
            .get()
            .addOnSuccessListener { document ->
                landmarkName = document.data?.get("name") as String
                landmarkDescription = document.data?.get("description") as String
                Log.w("name", landmarkName)
                Log.w("description", landmarkDescription)
                passData()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextIntent() {
        if (imgpath != null) {
            binding.btnOkay.visibility = View.VISIBLE
            binding.btnOkay.setOnClickListener {
                // 파이어베이스 스토리지 업로드 함수 실행
                val imgUri = imgpath.toUri()
                uploadImageTOFirebase(imgUri)
            }
        }
    }
    fun passData(){
        // 데이터를 ImgActivity로 옮김
        val nextIntent = Intent(this, ImgActivity::class.java)


        nextIntent.putExtra("image", imgpath)
        nextIntent.putExtra("landmarkName", landmarkName)
        nextIntent.putExtra("landmarkDescription", landmarkDescription)

        startActivity(nextIntent)
    }


}