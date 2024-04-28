package com.aqualinkbd.agrotraceprinting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aqualinkbd.agrotraceprinting.Models.TpData
import com.aqualinkbd.agrotraceprinting.Models.TpListBaseResponse
import com.charityright.bd.Utils.CustomSharedPref
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.zcs.sdk.DriverManager
import com.zcs.sdk.SdkResult
import com.zcs.sdk.print.PrnStrFormat
import com.zcs.sdk.print.PrnTextFont
import com.zcs.sdk.print.PrnTextStyle
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    lateinit var recyclerView : RecyclerView
    lateinit var progress: ProgressBar

    var mDriverManager = DriverManager.getInstance()
    var mSys = mDriverManager.baseSysDevice
    var mPrinter = mDriverManager.printer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initSdk()
        CustomSharedPref.init(this)
        getTpList()


        val appBar = findViewById<MaterialToolbar>(R.id.appBar)
        recyclerView = findViewById(R.id.recyclerView)
        progress = findViewById(R.id.progress_circular)
        recyclerView.layoutManager = LinearLayoutManager(this)

        appBar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.logoutMenu -> {
                    CustomSharedPref.write("Token","")
                    startActivity(Intent(this@MainActivity,LoginActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }

    }

    private fun getTpList() {

        val apiUrl = "http://agrotraces.com/api/v2/tobacco/tp"


        val client = OkHttpClient()


        val request = Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer ${CustomSharedPref.read("Token", "")}") // Add your header parameter
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {

                    val items = parseJsonResponse(responseBody)

                    runOnUiThread {
                        progress.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                        val adapter = items?.let { RecyclerViewAdapter(it,this@MainActivity) }
                        recyclerView.adapter = adapter
                    }
                }
            }
        })
    }

    private fun parseJsonResponse(responseBody: String): List<TpData>? {
        val gson = Gson()
        val apiResponse = gson.fromJson(responseBody, TpListBaseResponse::class.java)
        return  apiResponse.data
    }

    class RecyclerViewAdapter(private val list: List<TpData>,private val context: Context) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        var mDriverManager = DriverManager.getInstance()
        var mPrinter = mDriverManager.printer

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycle_item, parent, false)

            return ViewHolder(view)
        }

        // binds the list items to a view
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val ItemsViewModel = list[position]

            // sets the text to the textview from our itemHolder class
            holder.farmerName.text = "Farmer Name: ${ItemsViewModel.farmer_name}"
            holder.farmerReg.text = "Reg: ${ItemsViewModel.registration_no.toString()}"
            holder.baleCount.text = "Bale: ${ItemsViewModel.beal.toString()}"
            holder.farmerVillage.text = "Ext Center: ${ItemsViewModel.extension_center}"
            holder.tpDate.text = "Purchase Date: ${
                LocalDateTime.parse(ItemsViewModel.date.toString(), DateTimeFormatter.ISO_DATE_TIME).format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))}"


            holder.printBtn.setOnClickListener {
                println("Clicking")
                printTitle("GLOBAL LEAF TOBACCO COMPANY LTD")
                printNewLine()
                printNormal("Crop Year ${Calendar.YEAR}")
                printNormal("Tobacco Transport Permit")
                printNewLine()
                printQrcode(ItemsViewModel.tp_qr_code.toString())
                printNewLine()
                printTitle(ItemsViewModel.tp_no.toString())
                printNewLine()
                printDivider()
                printNewLine()
                printNormal("Name: ${ItemsViewModel.farmer_name}")
                printNormal("Father name: ${ItemsViewModel.farmer_father}")
                printNormal("Reg No: ${ItemsViewModel.registration_no}")
                printNormal("Tobacco Type: ${ItemsViewModel.tobacco_type}")
                printNormal("Tobacco Variety: ${ItemsViewModel.tobacco_variety}")
                printNormal("Ext Center: ${ItemsViewModel.extension_center}")
                printNormal("Allowed Bale: ${ItemsViewModel.beal}")
                printNormal("Buying Date: ${LocalDateTime.parse(ItemsViewModel.date.toString(), DateTimeFormatter.ISO_DATE_TIME).format(
                    DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}")
                printNewLine()
                printDivider()
                printNewLine()
                printNormal("Thank You!")
                printNormal("${LocalDateTime.parse(ItemsViewModel.date.toString(), DateTimeFormatter.ISO_DATE_TIME).format(
                    DateTimeFormatter.ofPattern("M/d/yyyy hh:mm a"))}")
                printNewLine()
            }

        }

        // return the number of the items in the list
        override fun getItemCount(): Int {
            return list.size
        }

        // Holds the views for adding it to image and text
        class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
            val farmerName: TextView = itemView.findViewById(R.id.farmerName)
            val farmerReg: TextView = itemView.findViewById(R.id.farmerReg)
            val baleCount: TextView = itemView.findViewById(R.id.baleCount)
            val farmerVillage: TextView = itemView.findViewById(R.id.farmerVillage)
            val tpDate: TextView = itemView.findViewById(R.id.tpDate)
            val printBtn: Button = itemView.findViewById(R.id.printBtn)
        }

        private fun printQrcode(qrString: String) {
            val printStatus = mPrinter.printerStatus
            if (printStatus != SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                mPrinter.setPrintAppendQRCode(qrString, 400, 400, Layout.Alignment.ALIGN_CENTER)
                mPrinter.setPrintStart()
            }
        }

        private fun printTitle(title: String) {
            val printStatus = mPrinter.printerStatus
            Toast.makeText(context, "" + printStatus, Toast.LENGTH_SHORT).show()
            if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                Toast.makeText(context, "Out Of Paper", Toast.LENGTH_SHORT).show()
            } else {
                val format = PrnStrFormat()
                format.textSize = 40
                format.ali = Layout.Alignment.ALIGN_CENTER
                format.style = PrnTextStyle.BOLD
                format.font = PrnTextFont.MONOSPACE
                mPrinter.setPrintAppendString(title, format)
                mPrinter.setPrintStart()
            }
        }

        private fun printNormal(text: String) {
            val printStatus = mPrinter.printerStatus
            if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                Toast.makeText(context, "Out Of Paper", Toast.LENGTH_SHORT).show()
            } else {
                val format = PrnStrFormat()
                format.textSize = 20
                format.ali = Layout.Alignment.ALIGN_CENTER
                format.style = PrnTextStyle.NORMAL
                format.font = PrnTextFont.MONOSPACE
                mPrinter.setPrintAppendString(text, format)
                mPrinter.setPrintStart()
            }
        }

        private fun printNewLine() {
            val printStatus = mPrinter.printerStatus
            if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                Toast.makeText(context, "Out Of Paper", Toast.LENGTH_SHORT).show()
            } else {
                val format = PrnStrFormat()
                format.textSize = 30
                format.ali = Layout.Alignment.ALIGN_CENTER
                format.style = PrnTextStyle.BOLD
                format.font = PrnTextFont.MONOSPACE
                mPrinter.setPrintAppendString("\n", format)
                mPrinter.setPrintStart()
            }
        }

        private fun printDivider() {
            val printStatus = mPrinter.printerStatus
            if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                Toast.makeText(context, "Out Of Paper", Toast.LENGTH_SHORT).show()
            } else {
                val format = PrnStrFormat()
                format.textSize = 25
                format.style = PrnTextStyle.NORMAL
                format.ali = Layout.Alignment.ALIGN_NORMAL
                mPrinter.setPrintAppendString(
                    "-----------------------------------------------------------------",
                    format
                )
                mPrinter.setPrintStart()
            }
        }

    }

    private fun initSdk() {
        var status = mSys.sdkInit()
        if (status != SdkResult.SDK_OK) {
            mSys.sysPowerOn()
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        status = mSys.sdkInit()
        if (status != SdkResult.SDK_OK) {
            Toast.makeText(this@MainActivity, "SDK Failed", Toast.LENGTH_SHORT).show()
        }
    }

}