package com.example.presensi_android_stp.views.profile

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.presensi_android_stp.BuildConfig
import com.example.presensi_android_stp.R
import com.example.presensi_android_stp.databinding.FragmentProfileBinding
import com.example.presensi_android_stp.dialog.MyDialog
import com.example.presensi_android_stp.hawkstorage.HawkStorage
import com.example.presensi_android_stp.model.LogoutResponse
import com.example.presensi_android_stp.networking.ApiServices
import com.example.presensi_android_stp.views.ubah_password.UbahPasswordActivity
import com.example.presensi_android_stp.views.login.LoginActivity
import com.example.presensi_android_stp.views.main.MainActivity
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileFragment : Fragment() {

    private var binding: FragmentProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClick()
        updateView()
    }
    private fun updateView() {
        val user = HawkStorage.instance(context).getUser()
        val imageUrl = BuildConfig.BASE_IMAGE_URL + user.foto
        Glide.with(requireContext()).load(imageUrl).placeholder(android.R.color.darker_gray).into(binding!!.ivProfile)
        binding?.tvNameProfile?.text = user.nama
        binding?.tvEmailProfile?.text = user.email
    }

    private fun onClick() {
        binding?.btnUbahPassword?.setOnClickListener{
            context?.startActivity<UbahPasswordActivity>()
        }

        binding?.btnUbahBahasa?.setOnClickListener{
            context?.toast("Ubah Bahasa")
        }
        binding?.btnLogout?.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.yakin))
                .setPositiveButton(getString(R.string.ya)){dialog, _ ->
                    logoutRequest(dialog)
                }
                .setNegativeButton(getString(R.string.tidak)){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun logoutRequest(dialog: DialogInterface?) {
        val token = HawkStorage.instance(context).getToken()
        MyDialog.showProgressDialog(context)
        ApiServices.getPresensiApi()
            .logoutRequest("Bearer $token")
            .enqueue(object : Callback<LogoutResponse>{
                override fun onResponse(
                    call: Call<LogoutResponse>,
                    response: Response<LogoutResponse>
                ) {
                    dialog?.dismiss()
                    MyDialog.hideDialog()
                    if (response.isSuccessful){
                        HawkStorage.instance(context).deleteAll()
                        (activity as MainActivity).finishAffinity()
                        context?.startActivity<LoginActivity>()
                    }else{
                        MyDialog.dynamicDialog(context, getString(R.string.peringatan), getString(R.string.ada_yang_salah))
                    }
                }

                override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                    dialog?.dismiss()
                    MyDialog.hideDialog()
                    MyDialog.dynamicDialog(context, getString(R.string.peringatan), "Error: ${t.message}")
                }

            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
