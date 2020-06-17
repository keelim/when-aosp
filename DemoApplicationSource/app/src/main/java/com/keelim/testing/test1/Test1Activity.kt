package com.keelim.testing.test1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.keelim.testing.R
import com.keelim.testing.result.ResultActivity
import com.keelim.testing.utils.BackPressCloseHandler
import kotlinx.android.synthetic.main.activity_test1.*


class Test1Activity : AppCompatActivity() {
    lateinit var test1Adapter: Test1Adapter
    lateinit var sampleDialog: AlertDialog
    lateinit var backPressCloseHandler: BackPressCloseHandler
    var resultArray = ArrayList<Long>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test1)
        backPressCloseHandler = BackPressCloseHandler(this)
        Toast.makeText(this, "샘플 버튼을 눌러 기능을 확인 하세요", Toast.LENGTH_SHORT).show()

        test1Adapter = Test1Adapter(arrayListOf())
        sampleDialog = AlertDialog.Builder(this)
            .setMessage("샘플 메시지를 눌려주셔서 감사합니다")
            .setPositiveButton("YES"){_, _ ->
                run {

                }
            }
            .setNegativeButton("No"){
                _, _ ->
                run {
                    Snackbar.make(test1_container, "샘플 다이알로그를 종료 합니다", Snackbar.LENGTH_SHORT).show()
                    sampleDialog.dismiss()
                }
            }
            .create()



        btn_result1.setOnClickListener {
            Toast.makeText(this, "3초 뒤 테스트를 시작합니다.", Toast.LENGTH_SHORT).show()
            Thread.sleep(3000)
            test1Start()
        }

        btn_result2.setOnClickListener {
            Snackbar.make(test1_container, "샘플 다이알로그를 실행 합니다.", Snackbar.LENGTH_SHORT).show()
            sampleDialog.show()
        }

        btn_result3.setOnClickListener {
            Snackbar.make(test1_container, "샘플 다이알로그를 종료 합니다", Snackbar.LENGTH_SHORT).show()
            sampleDialog.dismiss()
        }

    }

    private fun test1Start() {
        Snackbar.make(test1_container, "테스트1를 시작 합니다.", Snackbar.LENGTH_SHORT).show()
        measureTest1()
    }


    private fun measureTest1() {
        Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
        for (x in 0..10000) {

            val start = System.currentTimeMillis()
            Log.d("test1_start", "dialog start time: $start")

            val alert = AlertDialog.Builder(this)
                .setMessage("테스트 진행 중 $x")
                .create()
            alert.show()

            Thread.sleep(100)
            alert.dismiss()
            val end = System.currentTimeMillis()
            Log.d("test1_start", "dialog end time: $end")

            val time = end - start
            Log.d("test1 time", "test1 time:$time")

            val meanTime = time * 1000
            Toast.makeText(this, "측정 시간 입니다. $meanTime", Toast.LENGTH_SHORT).show()
            Thread.sleep(100)
            resultArray.add(time);
        }

        Snackbar.make(test1_container, "테스트를 종료 합니다. ", Snackbar.LENGTH_SHORT).show()
        Thread.sleep(100);

        endTest()
    }

    private fun endTest() {
        Intent(this, ResultActivity::class.java).apply {
            putExtra("test1", "data1")
            putExtra("result", resultArray)
            startActivity(this)
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            finish()
        }
    }

    override fun onBackPressed() {
        backPressCloseHandler.onBackPressed()
    }


}