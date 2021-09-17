package com.example.presensi_android_stp.views.riwayat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.presensi_android_stp.R
import com.example.presensi_android_stp.databinding.FragmentRiwayatBinding
import com.example.presensi_android_stp.date.MyDate.fromTimeStampToDate
import com.example.presensi_android_stp.date.MyDate.Kalender
import com.example.presensi_android_stp.date.MyDate.toDate
import com.example.presensi_android_stp.date.MyDate.toDay
import com.example.presensi_android_stp.date.MyDate.toMonth
import com.example.presensi_android_stp.date.MyDate.toTime
import com.example.presensi_android_stp.dialog.MyDialog
import com.example.presensi_android_stp.hawkstorage.HawkStorage
import com.example.presensi_android_stp.model.Riwayat
import com.example.presensi_android_stp.model.RiwayatResponse
import com.example.presensi_android_stp.networking.ApiServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class RiwayatFragment : Fragment() {

    private companion object{
        private val TAG: String = RiwayatFragment::class.java.simpleName
    }
    private var binding: FragmentRiwayatBinding? = null
    private val events = mutableListOf<EventDay>()
    private var dataHistories: List<Riwayat?>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        //Request Data History
        requestDataHistory()

        //Setup Calendar Swipe
        setupCalendar()

        //OnClick
        onClick()
    }

    private fun onClick() {
        binding?.kalenderRiwayat?.setOnDayClickListener(object : OnDayClickListener{
            override fun onDayClick(eventDay: EventDay) {
                val clickedDayCalendar = eventDay.calendar
                binding?.tvTanggalSekarang?.text = clickedDayCalendar.toDate().toDay()
                binding?.tvBulanSekarang?.text = clickedDayCalendar.toDate().toMonth()

                if (dataHistories != null){
                    for (dataHistory in dataHistories!!){
                        val checkInTime: String
                        val checkOutTime: String
                        val updateDate = dataHistory?.updatedAt
                        val calendarUpdated = updateDate?.fromTimeStampToDate()?.Kalender()
                        if (clickedDayCalendar.get(Calendar.DAY_OF_MONTH) == calendarUpdated?.get(Calendar.DAY_OF_MONTH)){
                            if (dataHistory.status == 1){
                                checkInTime = dataHistory.detail?.get(0)?.createdAt.toString()
                                checkOutTime = dataHistory.detail?.get(1)?.createdAt.toString()

                                binding?.tvWaktuMasuk?.text = checkInTime.fromTimeStampToDate()?.toTime()
                                binding?.tvTimePulang?.text = checkOutTime.fromTimeStampToDate()?.toTime()
                                break
                            }else{
                                checkInTime = dataHistory.detail?.get(0)?.createdAt.toString()
                                binding?.tvWaktuMasuk?.text = checkInTime.fromTimeStampToDate()?.toTime()
                                break
                            }
                        }else{
                            binding?.tvWaktuMasuk?.text = getString(R.string.default_text)
                            binding?.tvTimePulang?.text = getString(R.string.default_text)
                        }
                    }
                }
            }

        })
    }

    private fun setupCalendar() {
        binding?.kalenderRiwayat?.setOnPreviousPageChangeListener(object : OnCalendarPageChangeListener{
            override fun onChange() {
                requestDataHistory()
            }

        })

        binding?.kalenderRiwayat?.setOnForwardPageChangeListener(object : OnCalendarPageChangeListener{
            override fun onChange() {
                requestDataHistory()
            }

        })
    }

    private fun requestDataHistory() {
        val calendar = binding?.kalenderRiwayat?.currentPageDate
        val lastDay = calendar?.getActualMaximum(Calendar.DAY_OF_MONTH)
        val month = calendar?.get(Calendar.MONTH)?.plus(1)
        val year = calendar?.get(Calendar.YEAR)

        val fromDate = "$year-$month-01"
        val toDate = "$year-$month-$lastDay"
        getDataHistory(fromDate, toDate)
    }

    private fun getDataHistory(fromDate: String, toDate: String) {
        val token = HawkStorage.instance(context).getToken()
        binding?.pbHistory?.visibility = View.VISIBLE
        ApiServices.getPresensiApi()
            .getRiwayatPresensi("Bearer $token", fromDate, toDate)
            .enqueue(object : Callback<RiwayatResponse>{
                override fun onResponse(
                    call: Call<RiwayatResponse>,
                    response: Response<RiwayatResponse>
                ) {
                    binding?.pbHistory?.visibility = View.GONE
                    if (response.isSuccessful){
                        dataHistories = response.body()?.histories
                        if (dataHistories != null){
                            for (dataHistory in dataHistories!!){
                                val status = dataHistory?.status
                                val checkInTime: String
                                val checkOutTime: String
                                val calendarHistoryCheckIn: Calendar?
                                val calendarHistoryCheckOut: Calendar?
                                val currentDate = Calendar.getInstance()

                                if (status == 1){
                                    checkInTime = dataHistory.detail?.get(0)?.createdAt.toString()
                                    checkOutTime = dataHistory.detail?.get(1)?.createdAt.toString()

                                    calendarHistoryCheckOut = checkOutTime.fromTimeStampToDate()?.Kalender()

                                    if (calendarHistoryCheckOut != null){
                                        events.add(EventDay(calendarHistoryCheckOut, R.drawable.ic_baseline_check_circle_24_softblack))
                                    }

                                    if (currentDate.get(Calendar.DAY_OF_MONTH) == calendarHistoryCheckOut?.get(Calendar.DAY_OF_MONTH)){
                                        binding?.tvTanggalSekarang?.text = checkInTime.fromTimeStampToDate()?.toDay()
                                        binding?.tvBulanSekarang?.text = checkInTime.fromTimeStampToDate()?.toMonth()
                                        binding?.tvWaktuMasuk?.text = checkInTime.fromTimeStampToDate()?.toTime()
                                        binding?.tvTimePulang?.text = checkOutTime.fromTimeStampToDate()?.toTime()
                                    }
                                }else{
                                    checkInTime = dataHistory?.detail?.get(0)?.createdAt.toString()
                                    calendarHistoryCheckIn = checkInTime.fromTimeStampToDate()?.Kalender()

                                    if (calendarHistoryCheckIn != null){
                                        events.add(EventDay(calendarHistoryCheckIn, R.drawable.ic_baseline_check_circle_24_kuning))
                                    }

                                    if (currentDate.get(Calendar.DAY_OF_MONTH) == calendarHistoryCheckIn?.get(Calendar.DAY_OF_MONTH)){
                                        binding?.tvTanggalSekarang?.text = checkInTime.fromTimeStampToDate()?.toDay()
                                        binding?.tvBulanSekarang?.text = checkInTime.fromTimeStampToDate()?.toMonth()
                                        binding?.tvWaktuMasuk?.text = checkInTime.fromTimeStampToDate()?.toTime()
                                    }
                                }
                            }
                        }

                        binding?.kalenderRiwayat?.setEvents(events)
                    }else{
                        MyDialog.dynamicDialog(context, getString(R.string.peringatan), getString(R.string.ada_yang_salah))
                    }
                }

                override fun onFailure(call: Call<RiwayatResponse>, t: Throwable) {
                    binding?.pbHistory?.visibility = View.GONE
                    MyDialog.dynamicDialog(context, getString(R.string.peringatan), "${t.message}")
                    Log.e(TAG, "Error: ${t.message}")
                }

            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}