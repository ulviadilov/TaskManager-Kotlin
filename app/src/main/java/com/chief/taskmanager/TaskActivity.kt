package com.chief.taskmanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chief.taskmanager.databinding.ActivityTaskBinding
import com.google.android.material.snackbar.Snackbar

class TaskActivity : AppCompatActivity() {
    private lateinit var binding : ActivityTaskBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    var selectedBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        registerLauncher()
    }


    fun saveButton(view : View){
        val task = binding.taskText.text.toString()

    }

    fun selectImage(view : View){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            )
                {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                        Snackbar.make(view,"Permission Needed!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                    }else{
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }

    }


    private fun registerLauncher(){
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        val imageData = intentFromResult.data
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    this@TaskActivity.contentResolver,
                                    imageData!!
                                )
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                println(selectedBitmap)
                                binding.selectImage.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(
                                    this@TaskActivity.contentResolver,
                                    imageData
                                )
                                binding.selectImage.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    //permission granted
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    //permission denied
                    Toast.makeText(this@TaskActivity, "Permisson needed!", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun makeSmallerBitmap(image : Bitmap, maxSize: Int) : Bitmap{
        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble()/ height.toDouble()
        if (bitmapRatio> 1){
            width = maxSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }
        else{
            height = maxSize
            val scaledWeight = height / bitmapRatio
            width = scaledWeight.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }

}