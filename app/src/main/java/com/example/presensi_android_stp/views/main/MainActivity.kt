package com.example.presensi_android_stp.views.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.presensi_android_stp.R
import com.example.presensi_android_stp.databinding.ActivityMainBinding
import com.example.presensi_android_stp.views.presensi.PresensiFragment
import com.example.presensi_android_stp.views.riwayat.RiwayatFragment
import com.example.presensi_android_stp.views.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

    }
    private fun init(){
        binding.btmNavMain.setOnNavigationItemSelectedListener{
            when(it.itemId) {
                R.id.action_riwayat -> {
                    openFragment(RiwayatFragment())
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.action_presensi -> {
                    openFragment(PresensiFragment())
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.action_profile -> {
                    openFragment(ProfileFragment())
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
        openHomeFragment()
    }

    private fun openHomeFragment() {
        binding.btmNavMain.selectedItemId = R.id.action_presensi
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_main, fragment)
            .addToBackStack(null)
            .commit()
    }
}