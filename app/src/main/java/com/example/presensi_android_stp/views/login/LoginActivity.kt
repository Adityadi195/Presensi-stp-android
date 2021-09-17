package com.example.presensi_android_stp.views.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.example.presensi_android_stp.R
import com.example.presensi_android_stp.databinding.ActivityLoginBinding
import com.example.presensi_android_stp.hawkstorage.HawkStorage
import com.example.presensi_android_stp.dialog.MyDialog
import com.example.presensi_android_stp.model.LoginResponse
import com.example.presensi_android_stp.networking.ApiServices
import com.example.presensi_android_stp.networking.RetrofitNetworkingClient
import com.example.presensi_android_stp.views.lupa_password.LupaPasswordActivity
import com.example.presensi_android_stp.views.main.MainActivity
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onClick()
    }

    private fun onClick() {
        binding.btnLogin.setOnClickListener{
            val email = binding.etEmailLogin.text.toString()
            val password = binding.etPasswordLogin.text.toString()
            if (isFormValid(email, password)){
                loginToServer(email, password)
            }
        }

        binding.lupaPassword.setOnClickListener(){
            startActivity<LupaPasswordActivity>()
        }
    }

    private fun loginToServer(email: String, password: String) {
        val loginRequest = LoginRequest(email = email, password = password, perangkat = "mobile")
        val loginRequestString = Gson().toJson(loginRequest)
        MyDialog.showProgressDialog(this)

        ApiServices.getPresensiApi()
            .loginRequest(loginRequestString)
            .enqueue(object : Callback<LoginResponse>{
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: retrofit2.Response<LoginResponse>
                ) {
                    MyDialog.hideDialog()
                    if (response.isSuccessful){
                        val user = response.body()?.user
                        val token = response.body()?.meta?.token
                        if (user != null && token != null){
                            HawkStorage.instance(this@LoginActivity).setUser(user)
                            HawkStorage.instance(this@LoginActivity).setToken(token)
                            goToMain()
                        }
                    }else{
                        val errorConverter: Converter<ResponseBody, LoginResponse> =
                            RetrofitNetworkingClient
                                .getClient()
                                .responseBodyConverter(
                                    LoginResponse::class.java,
                                    arrayOfNulls<Annotation>(0)
                                )
                        var errorResponse: LoginResponse?
                        try {
                            response.errorBody()?.let {
                                errorResponse = errorConverter.convert(it)
                                MyDialog.dynamicDialog(
                                    this@LoginActivity,
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


                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    MyDialog.hideDialog()
                    Log.e(TAG, "Error: ${t.message}")
                }
            })
    }

    private fun goToMain() {
        startActivity<MainActivity>()
        finishAffinity()
    }

    private fun isFormValid(email: String, password: String): Boolean {
        if (email.isEmpty()){
            binding.etEmailLogin.error = getString(R.string.masukan_email)
            binding.etEmailLogin.requestFocus()
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmailLogin.error = getString(R.string.masukan_email_yang_benar)
            binding.etEmailLogin.requestFocus()
        } else if(password.isEmpty()){
            binding.etEmailLogin.error = null
            binding.etPasswordLogin.error = getString(R.string.masukan_password)
            binding.etPasswordLogin.requestFocus()
        }else{
            binding.etEmailLogin.error = null
            binding.etPasswordLogin.error = null
            return true
        }
        return false
    }

    companion object{
        private val TAG = LoginActivity::class.java.simpleName
    }

}