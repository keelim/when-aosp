package com.keelim.testing.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.keelim.testing.R
import com.keelim.testing.test1.Test1Activity
import com.keelim.testing.test2.Test2Activity
import com.keelim.testing.utils.BackPressCloseHandler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1
    lateinit var backPressCloseHandler: BackPressCloseHandler

    private val permissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            Toast.makeText(this@MainActivity, "모든 권한이 승인 되었습니다.", Toast.LENGTH_SHORT).show()
        }

        override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
            Toast.makeText(this@MainActivity, "모든 권한이 승인 되지 않았습니다. 종료합니다.", Toast.LENGTH_SHORT)
                .show()
            Thread.sleep(3000)
            finish()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        backPressCloseHandler = BackPressCloseHandler(this)
        Toast.makeText(this, "권한 확인 중입니다.", Toast.LENGTH_SHORT).show()


        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setDeniedMessage("권한을 승인하지 않으시면 어플리케이션이 종료됩니다.")
            .setPermissions(android.Manifest.permission.INTERNET)
            .setPermissions(android.Manifest.permission.SYSTEM_ALERT_WINDOW)
            .setPermissions(android.Manifest.permission.FOREGROUND_SERVICE)
            .check()

        overayCheck()


        val list = ArrayList<String>()
        for (i in 0..99) {
            list.add(String.format("TEXT %d", i))
        }

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        val recyclerView = findViewById<RecyclerView>(R.id.main_recycler)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        // 리사이클러뷰에 SimpleTextAdapter 객체 지정.
//
//        // 리사이클러뷰에 SimpleTextAdapter 객체 지정.
//        val adapter = MainAdapter(list)b
//        recyclerView.adapter = adapter

        btn_test1.setOnClickListener {
            val test1 = Intent(this, Test1Activity::class.java)
            startActivity(test1)
            finish()

        }

        btn_test2.setOnClickListener {
            val test2 = Intent(this, Test2Activity::class.java)
            startActivity(test2)
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);


            finish()

        }

    }

    override fun onBackPressed() {
        backPressCloseHandler.onBackPressed()
    }

    private fun overayCheck(){
        if(!Settings.canDrawOverlays(this)){
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "요구조건이 충족되지 않아 테스트를 할 수 없습니다. 종료합니다.", Toast.LENGTH_SHORT).show()
                Thread.sleep(3000)
                finish()

            }
        }
    }
}