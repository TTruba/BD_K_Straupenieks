package com.kstraupenieks.asca

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast

class RecentScamFragment : Fragment() {

    private val REQUEST_CODE_CALL_LOG = 101
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScamCallAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recent_scams, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewRecentScams)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG), REQUEST_CODE_CALL_LOG)
        } else {
            loadRecentScamCalls()
        }

        return view
    }

    private fun loadRecentScamCalls() {
        val scamCalls = mutableListOf<ScamCall>()

        val cursor = requireContext().contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            "${CallLog.Calls.TYPE} = ?",
            arrayOf(CallLog.Calls.BLOCKED_TYPE.toString()),
            "${CallLog.Calls.DATE} DESC"
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)

            while (it.moveToNext()) {
                val number = it.getString(numberIndex)
                val date = it.getLong(dateIndex)
                val type = it.getInt(typeIndex)

                scamCalls.add(ScamCall(number, date, type))
            }
        }

        adapter = ScamCallAdapter(scamCalls)
        recyclerView.adapter = adapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_CALL_LOG && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadRecentScamCalls()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
