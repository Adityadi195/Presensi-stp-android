package com.example.presensi_android_stp.views.ubah_password

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.presensi_android_stp.R
import com.example.presensi_android_stp.databinding.ActivityUbahPasswordBinding
import com.example.presensi_android_stp.dialog.MyDialog
import com.example.presensi_android_stp.hawkstorage.HawkStorage
import com.example.presensi_android_stp.model.UbahPasswordResponse
import com.example.presensi_android_stp.model.LoginResponse
import com.example.presensi_android_stp.networking.ApiServices
import com.example.presensi_android_stp.networking.RetrofitNetworkingClient
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException

class UbahPasswordActivity : AppCompatActivity() {

    companion object{
        private val TAG = UbahPasswordActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityUbahPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUbahPasswordBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_ubah_password)

        init()
        onClick()
    }

    private fun onClick() {
        binding.tbUbahPassword.setNavigationOnClickListener{
            finish()
        }


        binding.btnUbahPassword.setOnClickListener {
            val oldPass = binding.etPasswordLama.text.toString()
            val newPass = binding.etPasswordBaru.text.toString()
            val konfirmNewPass = binding.etKonfirmasiPasswordBaru.text.toString()
            if (checkValidation(oldPass, newPass, konfirmNewPass)){
                ubahPassToServer(oldPass, newPass, konfirmNewPass)
            }
        }
    }

    private fun ubahPassToServer(oldPass: String, newPass: String, confirmNewPass: String) {
        val token = HawkStorage.instance(this).getToken()
        val changePassRequest = UbahPasswordRequest(
            passwordOld = oldPass,
            password = newPass,
            passwordConfirmation = confirmNewPass
        )
        val changePassRequestString = Gson().toJson(changePassRequest)
        MyDialog.showProgressDialog(this)
        ApiServices.getPresensiApi()
            .ubahPassword("Bearer $token", changePassRequestString)
            .enqueue(object : Callback<UbahPasswordResponse>{
                override fun onResponse(
                    call: Call<UbahPasswordResponse>,
                    response: Response<UbahPasswordResponse>
                ) {
                    MyDialog.hideDialog()
                    if (response.isSuccessful){
                        MyDialog.dynamicDialog(
                            this@UbahPasswordActivity,
                            getString(R.string.berhasil),
                            getString(R.string.password_berhasil_ubah)
                        )
                        Handler(Looper.getMainLooper()).postDelayed({
                            MyDialog.hideDialog()
                            finish()
                        },2000)
                    }else{
                        val errorConverter: Converter<ResponseBody, UbahPasswordResponse> =
                            RetrofitNetworkingClient
                                .getClient()
                                .responseBodyConverter(
                                    LoginResponse::class.java,
                                    arrayOfNulls<Annotation>(0)
                                )
                        var errorResponse: UbahPasswordResponse?
                        try {
                            response.errorBody()?.let {
                                errorResponse = errorConverter.convert(it)
                                MyDialog.dynamicDialog(this@UbahPasswordActivity, getString(R.string.gagal), errorResponse?.message.toString())
                            }
                        }catch (e: IOException){
                            Log.e(TAG, "Error: ${e.message}")
                        }
                    }
                }

                override fun onFailure(call: Call<UbahPasswordResponse>, t: Throwable) {
                    MyDialog.hideDialog()
                    MyDialog.dynamicDialog(this@UbahPasswordActivity, getString(R.string.peringatan), "Error: ${t.message}")
                }

            })
    }

    private fun checkValidation(oldPass: String, newPass: String, confirmNewPass: String): Boolean {
        when {
            oldPass.isEmpty() -> {
                binding.etPasswordLama.error = getString(R.string.masukan_password)
                binding.etPasswordLama.requestFocus()
            }
            newPass.isEmpty() -> {
                binding.etPasswordBaru.error = getString(R.string.masukan_password)
                binding.etPasswordBaru.requestFocus()
            }
            confirmNewPass.isEmpty() -> {
                binding.etKonfirmasiPasswordBaru.error = getString(R.string.masukan_password)
                binding.etKonfirmasiPasswordBaru.requestFocus()
            }
            newPass != confirmNewPass -> {
                binding.etPasswordBaru.error = getString(R.string.password_tidak_sama)
                binding.etPasswordBaru.requestFocus()
                binding.etKonfirmasiPasswordBaru.error = getString(R.string.password_tidak_sama)
                binding.etKonfirmasiPasswordBaru.requestFocus()
            }
            else -> {
                binding.etPasswordBaru.error = null
                binding.etKonfirmasiPasswordBaru.error = null
                return true
            }
        }
        return false
    }

    private fun init(){
        setSupportActionBar(binding.tbUbahPassword)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
