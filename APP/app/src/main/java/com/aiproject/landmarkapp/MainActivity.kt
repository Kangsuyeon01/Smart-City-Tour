package com.aiproject.landmarkapp

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var isFabOpen = false
    private var auth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null
    var Camera_start  = ""
    var Gallery_start = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar.title = "Tour Seoul"
        // 플로팅 버튼 클릭시 에니메이션 동작 기능
        fabMain.setOnClickListener {
            toggleFab()
        }

        // 플로팅 버튼 클릭 이벤트 - 수정
        fab_logout.setOnClickListener {
            Toast.makeText(this, "logout", Toast.LENGTH_SHORT).show()
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()

            // 로그아웃
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            auth?.signOut()

        }

        btn_maps.setOnClickListener {
            val intent = Intent(this, NaverMapsActivity::class.java)
            startActivity(intent)
        }

        btn_camera.setOnClickListener {
            Camera_start ="true"
            Gallery_start = "false"
            val intent = Intent(this, SocketActivity::class.java)
            intent.putExtra("Camera_start", Camera_start)
            intent.putExtra("Gallery_start", Gallery_start)
            startActivity(intent)


        }
        btn_gallery.setOnClickListener {
            Gallery_start = "true"
            Camera_start = "false"
            val intent = Intent(this, SocketActivity::class.java)
            intent.putExtra("Camera_start", Camera_start)
            intent.putExtra("Gallery_start", Gallery_start)
            startActivity(intent)
        }


        btn_guide.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("How to use?")
                .setMessage("Choose a picture of Seoul's landmark.\n" +
                        "And upload it to this application.\n" +
                        "Then, it will automatically recognize the landmarks in the picture.")
                .setPositiveButton("I got it",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
            // 다이얼로그를 띄워주기
            builder.show()
        }
    }


    /**t
     *  플로팅 액션 버튼 클릭시 동작하는 애니메이션 효과 세팅
     */
    private fun toggleFab() {
//        Toast.makeText(this, "메인 플로팅 버튼 클릭 : $isFabOpen", Toast.LENGTH_SHORT).show()

        // 플로팅 액션 버튼 닫기 - 열려있는 플로팅 버튼 집어넣는 애니메이션 세팅
        if (isFabOpen) {
            ObjectAnimator.ofFloat(fab_logout, "translationY", 0f).apply { start() }
            fabMain.setImageResource(R.drawable.smallup_24)

            // 플로팅 액션 버튼 열기 - 닫혀있는 플로팅 버튼 꺼내는 애니메이션 세팅
        } else {
            ObjectAnimator.ofFloat(fab_logout, "translationY", -200f).apply { start() }
            fabMain.setImageResource(R.drawable.smalldown_24)
        }

        isFabOpen = !isFabOpen

    }
}