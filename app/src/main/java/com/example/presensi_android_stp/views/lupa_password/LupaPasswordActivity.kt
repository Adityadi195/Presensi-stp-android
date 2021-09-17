package com.example.presensi_android_stp.views.lupa_password

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.presensi_android_stp.R
import com.example.presensi_android_stp.databinding.ActivityLupaPasswordBinding
import com.example.presensi_android_stp.dialog.MyDialog
import com.example.presensi_android_stp.model.LupaPasswordResponse
import com.example.presensi_android_stp.networking.ApiServices
import com.example.presensi_android_stp.networking.RetrofitNetworkingClient
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException

class LupaPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLupaPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLupaPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        onClick()
    }

    private fun onClick() {
        binding.tbLupaPassword.setNavigationOnClickListener {
            finish()
        }

        binding.btnLupaPassword.setOnClickListener {
            val email = binding.etEmailLupaPassword.text.toString()
            if (isFormValid(email)){
                lupa_passwordToServer(email)
            }
        }
    }

    private fun lupa_passwordToServer(email: String) {
        val LupaPasswordrequest = LupaPasswordResponse(email = email)
        val LupaPasswordRequestString = Gson().toJson(LupaPasswordrequest)

        MyDialog.showProgressDialog(this)

        ApiServices.getPresensiApi()
            .lupaPasswordRequest(LupaPasswordRequestString)
            .enqueue(object : Callback<LupaPasswordResponse>{
                override fun onResponse(
                    call: Call<LupaPasswordResponse>,
                    response: Response<LupaPasswordResponse>
                ) {
                    MyDialog.hideDialog()
                    if (response.isSuccessful){
                        val message = response.body()?.message
                        MyDialog.dynamicDialog(
                            this@LupaPasswordActivity,
                            getString(R.string.berhasil),
                            message.toString()
                        )
                        Handler(Looper.getMainLooper()).postDelayed({
                            MyDialog.hideDialog()
                            finish()
                        },2000)
                    }else{
                        val errorConverter: Converter<ResponseBody, LupaPasswordResponse> =
                            RetrofitNetworkingClient
                                .getClient()
                                .responseBodyConverter(
                                    LupaPasswordResponse::class.java,
                                    arrayOfNulls<Annotation>(0)
                                )
                        var errorResponse: LupaPasswordResponse?
                        try {
                            response.errorBody()?.let {
                                errorResponse = errorConverter.convert(it)
                                MyDialog.dynamicDialog(
                                    this@LupaPasswordActivity,
                                    getString(R.string.gagal),
                                    errorResponse?.message.toString()
                                )
                            }
                        }catch (e: IOException){
                            e.printStackTrace()
                            Log.e(TAG, "Error: ${e.message}")
                        }
                    }
                }

                override fun onFailure(call: Call<LupaPasswordResponse>, t: Throwable) {
                    MyDialog.hideDialog()
                    Log.e(TAG, "Error: ${t.message}")
                }

            })
    }

    private fun isFormValid(email: String): Boolean {
        if (email.isEmpty()){
            binding.etEmailLupaPassword.error = getString(R.string.masukan_email)
            binding.etEmailLupaPassword.requestFocus()
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmailLupaPassword.error = getString(R.string.masukan_email_yang_benar)
            binding.etEmailLupaPassword.requestFocus()
        }else{
            binding.etEmailLupaPassword.error = null
            return true
        }
        return false
    }

    private fun init() {
        setSupportActionBar(binding.tbLupaPassword)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    companion object{
        private val TAG = LupaPasswordActivity::class.java.simpleName
    }
}
