package com.example.presensi_android_stp.views.presensi

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.presensi_android_stp.BuildConfig
import com.example.presensi_android_stp.R
import com.example.presensi_android_stp.databinding.BsPresensiBinding
import com.example.presensi_android_stp.databinding.FragmentPresensiBinding
import com.example.presensi_android_stp.date.MyDate
import com.example.presensi_android_stp.dialog.MyDialog
import com.example.presensi_android_stp.hawkstorage.HawkStorage
import com.example.presensi_android_stp.model.PresensiResponse
import com.example.presensi_android_stp.model.RiwayatResponse
import com.example.presensi_android_stp.networking.ApiServices
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PresensiFragment : Fragment(), OnMapReadyCallback {

    companion object{
        private const val REQUEST_CODE_MAP_PERMISSIONS = 1000
        private const val REQUEST_CODE_CAMERA_PERMISSIONS = 1001
        private const val REQUEST_CODE_LOCATION = 2000
        private const val REQUEST_CODE_IMAGE_CAPTURE = 2001
        private val TAG = PresensiFragment::class.java.simpleName
    }

    private val mapsPeta = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val kameraperms = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    //Config Maps
    private var map_Presensi: SupportMapFragment? = null
    private var map: GoogleMap? = null
    private var lokasi_Manager: LocationManager? = null
    private var lokasi_Request: LocationRequest? = null
    private var lokasi_SettingsRequest: LocationSettingsRequest? = null
    private var set_Client: SettingsClient? = null
    private var lokasi_Now: Location? = null
    private var lokasi_CallBack: LocationCallback? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    //UI
    private var binding: FragmentPresensiBinding? = null
    private var bindingBottomSheet: BsPresensiBinding? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private var Foto_Path = ""
    private var Masuk = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        binding = FragmentPresensiBinding.inflate(inflater, container, false)
        bindingBottomSheet = binding?.layoutBtmSheet
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        bindingBottomSheet = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (lokasi_Now != null && lokasi_CallBack != null){
            fusedLocationProviderClient?.removeLocationUpdates(lokasi_CallBack)
        }
    }

    override fun onResume() {
        super.onResume()
        checkIfAlreadyPresent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupMaps()
        onClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE){
            if (resultCode == RESULT_OK){
                if (Foto_Path.isNotEmpty()){
                    val uri = Uri.parse(Foto_Path)
                    bindingBottomSheet?.ivAmbilFoto?.setImageURI(uri)
                    bindingBottomSheet?.ivAmbilFoto?.adjustViewBounds = true
                }
            }else{
                if (Foto_Path.isNotEmpty()){
                    val file = File(Foto_Path)
                    file.delete()
                    Foto_Path = ""
                    context?.toast(getString(R.string.gagal_mengambil_gambar))
                }
            }
        }
    }

    private fun onClick() {
        binding?.fabGetLokasiSekarang?.setOnClickListener {
            Lokasi_Sekarang()
        }

        bindingBottomSheet?.ivAmbilFoto?.setOnClickListener {
            if (cekKamera()){
                openCamera()
            }else{
                setRequestPermissionCamera()
            }
        }

        bindingBottomSheet?.btnMasuk?.setOnClickListener {
            val token = HawkStorage.instance(context).getToken()
            if (checkValidation()){
                if (Masuk){
                    AlertDialog.Builder(context)
                        .setTitle(getString(R.string.yakin))
                        .setPositiveButton(getString(R.string.ya)){ _ , _ ->
                            kirimDataAbsen(token, "out")
                        }
                        .setNegativeButton(getString(R.string.tidak)){dialog, _ ->

                            dialog.dismiss()
                        }
                        .show()
                }else{
                    AlertDialog.Builder(context)
                        .setTitle(getString(R.string.yakin))
                        .setPositiveButton(getString(R.string.ya)){ _ , _ ->
                            kirimDataAbsen(token, "in")
                        }
                        .setNegativeButton(getString(R.string.tidak)){dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

    private fun kirimDataAbsen(token: String, type: String) {
        val params = HashMap<String, RequestBody>()
        MyDialog.showProgressDialog(context)
        if (lokasi_Now != null && Foto_Path.isNotEmpty()){
            val latitude = lokasi_Now?.latitude.toString()
            val longitude = lokasi_Now?.longitude.toString()
            val lokasi = bindingBottomSheet?.tvLokasiSekarang?.text.toString()

            val file = File(Foto_Path)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                BuildConfig.APPLICATION_ID + ".fileprovider",
                file
            )
            val typeFile = context?.contentResolver?.getType(uri)

            val mediaTypeText = MultipartBody.FORM
            val mediaTypeFile = typeFile?.toMediaType()

            val requestLatitude = latitude.toRequestBody(mediaTypeText)
            val requestLongitude = longitude.toRequestBody(mediaTypeText)
            val requestLokasi = lokasi.toRequestBody(mediaTypeText)
            val requestType = type.toRequestBody(mediaTypeText)

            params["lat"] = requestLatitude
            params["long"] = requestLongitude
            params["lokasi"] = requestLokasi
            params["type"] = requestType

            val requestFotoFile = file.asRequestBody(mediaTypeFile)
            val multipartBody = MultipartBody.Part.createFormData("foto", file.name, requestFotoFile)
            ApiServices.getPresensiApi()
                .presensi("Bearer $token", params, multipartBody)
                .enqueue(object : Callback<PresensiResponse> {
                    override fun onResponse(
                        call: Call<PresensiResponse>,
                        response: Response<PresensiResponse>
                    ) {
                        MyDialog.hideDialog()
                        if (response.isSuccessful){
                            val presensiResponse = response.body()
                            Foto_Path = ""
                            bindingBottomSheet?.ivAmbilFoto?.setImageDrawable(
                                ContextCompat.getDrawable(context!!, R.drawable.ic_baseline_add_circle_24)
                            )
                            bindingBottomSheet?.ivAmbilFoto?.adjustViewBounds = false

                            if (type == "in"){
                                MyDialog.dynamicDialog(context, getString(R.string.absen_berhasil), presensiResponse?.message.toString())
                            }else{
                                MyDialog.dynamicDialog(context, getString(R.string.pulang_berhasil), presensiResponse?.message.toString())
                            }
                            checkIfAlreadyPresent()
                        }else{
                            MyDialog.dynamicDialog(context, getString(R.string.peringatan), getString(R.string.ada_yang_salah))
                        }
                    }

                    override fun onFailure(call: Call<PresensiResponse>, t: Throwable) {
                        MyDialog.hideDialog()
                        Log.e(TAG, "Error: ${t.message}")
                    }

                })
        }
    }

    private fun checkIfAlreadyPresent() {
        val token = HawkStorage.instance(context).getToken()
        val currentDate = MyDate.getCurrentDateForServer()

        ApiServices.getPresensiApi()
            .getRiwayatPresensi("Bearer $token", currentDate, currentDate)
            .enqueue(object : Callback<RiwayatResponse>{
                override fun onResponse(
                    call: Call<RiwayatResponse>,
                    response: Response<RiwayatResponse>
                ) {
                    if (response.isSuccessful){
                        val histories = response.body()?.histories
                        if (histories != null && histories.isNotEmpty()){
                            if (histories[0]?.status == 1){
                                Masuk = false
                                checkIsCheckIn()
                                bindingBottomSheet?.btnMasuk?.isEnabled = false
                                bindingBottomSheet?.btnMasuk?.text = getString(R.string.telah_melakukan_absen)
                            }else{
                                Masuk = true
                                checkIsCheckIn()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<RiwayatResponse>, t: Throwable) {
                    Log.e(TAG, "Error: ${t.message}")
                }

            })
    }

    private fun checkIsCheckIn() {
        if (Masuk){
            bindingBottomSheet?.btnMasuk?.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_pulang)
            bindingBottomSheet?.btnMasuk?.text = getString(R.string.pulang)
        }else{
            bindingBottomSheet?.btnMasuk?.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_button_primary)
            bindingBottomSheet?.btnMasuk?.text = getString(R.string.masuk)
        }
    }

    private fun checkValidation(): Boolean {
        if (Foto_Path.isEmpty()){
            MyDialog.dynamicDialog(context, getString(R.string.peringatan), getString(R.string.tolong_ambil_foto))
            return false
        }
        return true
    }

    private fun openCamera() {
        context?.let { context ->
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(context.packageManager) != null){
                val fotoFile = try {
                    createImageFile()
                }catch (ex: IOException){
                    null
                }
                fotoFile?.also {
                    val fotoUri = FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        it
                    )
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
                    startActivityForResult(cameraIntent, REQUEST_CODE_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            Foto_Path = absolutePath
        }
    }

    private fun init() {
        //Setup Location
        lokasi_Manager = context?.getSystemService(LOCATION_SERVICE) as LocationManager
        set_Client = LocationServices.getSettingsClient(requireContext())
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        lokasi_Request = LocationRequest()
            .setInterval(10000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val builder = LocationSettingsRequest.Builder().addLocationRequest(lokasi_Request!!)
        lokasi_SettingsRequest = builder.build()

        //Setup BottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bindingBottomSheet!!.bsPresensi)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_CODE_MAP_PERMISSIONS -> {
                var isHasPermission = false
                val permissionNotGranted = StringBuilder()

                for (i in permissions.indices){
                    isHasPermission = grantResults[i] == PackageManager.PERMISSION_GRANTED

                    if (!isHasPermission){
                        permissionNotGranted.append("${permissions[i]}\n")
                    }
                }

                if (isHasPermission){
                    setupMaps()
                }else{
                    val message = permissionNotGranted.toString() + "\n" + getString(R.string.tidak_diizinkan)
                    MyDialog.dynamicDialog(context, getString(R.string.required_permission), message)
                }
            }

            REQUEST_CODE_CAMERA_PERMISSIONS -> {
                var isHasPermission = false
                val permissionNotGranted = StringBuilder()

                for (i in permissions.indices){
                    isHasPermission = grantResults[i] == PackageManager.PERMISSION_GRANTED

                    if (!isHasPermission){
                        permissionNotGranted.append("${permissions[i]}\n")
                    }
                }

                if (isHasPermission){
                    openCamera()
                }else{
                    val message = permissionNotGranted.toString() + "\n" + getString(R.string.tidak_diizinkan)
                    MyDialog.dynamicDialog(context, getString(R.string.required_permission), message)
                }
            }
        }
    }

    private fun setupMaps() {
        map_Presensi = childFragmentManager.findFragmentById(R.id.map_presensi) as SupportMapFragment
        map_Presensi?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        if (checkPermission()){
            //Coordinate Kantor Codepolitan
            val stp = LatLng(-6.879513, 107.590085)
            map?.moveCamera(CameraUpdateFactory.newLatLng(stp))
            map?.animateCamera(CameraUpdateFactory.zoomTo(20f))

            Lokasi_Sekarang()
        }else{
            setRequestPermission()
        }
    }

    private fun Lokasi_Sekarang() {
        bindingBottomSheet?.tvLokasiSekarang?.text = getString(R.string.mencari_lokasi)
        if (checkPermission()){
            if (isLocationEnabled()){
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = false

                lokasi_CallBack = object : LocationCallback(){
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)
                        lokasi_Now = locationResult?.lastLocation

                        if (lokasi_Now != null){
                            val latitude = lokasi_Now?.latitude
                            val longitude = lokasi_Now?.longitude

                            if (latitude != null && longitude != null){
                                val latLng = LatLng(latitude,longitude)
                                map?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                                map?.animateCamera(CameraUpdateFactory.zoomTo(20F))

                                val lokasi = getLokasi(latitude, longitude)
                                if (lokasi != null && lokasi.isNotEmpty()){
                                    bindingBottomSheet?.tvLokasiSekarang?.text = lokasi
                                }
                            }
                        }
                    }
                }
                fusedLocationProviderClient?.requestLocationUpdates(
                    lokasi_Request,
                    lokasi_CallBack,
                    Looper.myLooper()
                )
            }else{
                goToTurnOnGps()
            }
        }else{
            setRequestPermission()
        }
    }

    private fun getLokasi(latitude: Double, longitude: Double): String? {
        val result: String
        context?.let {
            val geocode = Geocoder(it, Locale.getDefault())
            val addresses = geocode.getFromLocation(latitude, longitude, 1)

            if (addresses.size > 0){
                result = addresses[0].getAddressLine(0)
                return result
            }
        }
        return null
    }

    private fun goToTurnOnGps() {
        set_Client?.checkLocationSettings(lokasi_SettingsRequest)
            ?.addOnSuccessListener {
                Lokasi_Sekarang()
            }?.addOnFailureListener{
                when((it as ApiException).statusCode){
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            val resolvableApiException = it as ResolvableApiException
                            resolvableApiException.startResolutionForResult(
                                activity,
                                REQUEST_CODE_LOCATION
                            )
                        } catch (ex: IntentSender.SendIntentException){
                            ex.printStackTrace()
                            Log.e(TAG, "Error: ${ex.message}")
                        }
                    }
                }
            }
    }

    private fun isLocationEnabled(): Boolean {
        if (lokasi_Manager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!! ||
            lokasi_Manager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)!!){
            return true
        }
        return false
    }

    private fun checkPermission(): Boolean {
        var isHasPermission = false
        context?.let {
            for (permission in mapsPeta){
                isHasPermission = ActivityCompat.checkSelfPermission(it, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
        return isHasPermission
    }

    private fun setRequestPermission() {
        requestPermissions(mapsPeta, REQUEST_CODE_MAP_PERMISSIONS)
    }

    private fun setRequestPermissionCamera() {
        requestPermissions(kameraperms, REQUEST_CODE_CAMERA_PERMISSIONS)
    }

    private fun cekKamera(): Boolean {
        var isHasPermission = false
        context?.let {
            for (permission in kameraperms){
                isHasPermission = ActivityCompat.checkSelfPermission(it, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
        return isHasPermission
    }
}