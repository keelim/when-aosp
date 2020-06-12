package com.keelim.testing.result

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.keelim.testing.R
import com.keelim.testing.utils.BackPressCloseHandler
import kotlinx.android.synthetic.main.activity_result.*
import java.util.*

class ResultActivity : AppCompatActivity() {
    lateinit var resultArray: ArrayList<Long>
    lateinit var backPressCloseHandler: BackPressCloseHandler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        backPressCloseHandler = BackPressCloseHandler(this)
        result_progress.visibility = View.GONE

        AlertDialog.Builder(this) // Dialog 를 띄웁니다.
            .setMessage("결과를 전송")
            .setNegativeButton("아니오") { _, _ ->
                Toast.makeText(this, "결과를 취소하고 어플리케이션을 종료 합니다. 다시 실행 바랍니다.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
            .setPositiveButton("예") { _, _ ->
                Toast.makeText(this, "성능 측정 결과를 받아옵니다. . ", Toast.LENGTH_SHORT).show()
                checkIntent()


                // 파일 쓰기
            }
            .create()
            .show()

    }


    private fun checkIntent() {
        result_progress.visibility = View.VISIBLE
        resultArray = intent.getSerializableExtra("result") as ArrayList<Long>
        Snackbar.make(result_container, "화면이 가만히 있을 때까지 만지지 말아주세요", Snackbar.LENGTH_SHORT).show()
        for (x in resultArray) {
            Toast.makeText(this, "$x", Toast.LENGTH_SHORT).show()
            Thread.sleep(10)
        }
        Snackbar.make(result_container, "검증 완료 했습니다.", Snackbar.LENGTH_SHORT).show()

        result_progress.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        backPressCloseHandler.onBackPressed()
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}