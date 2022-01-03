package com.nubari.favdish.view.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nubari.favdish.R
import android.view.View
import com.karumi.dexter.Dexter
import com.nubari.favdish.databinding.ActivityAddUpdateDishBinding
import com.nubari.favdish.databinding.DialogCustomImageSelectionBinding
import android.Manifest
import android.Manifest.permission.CAMERA
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.audiofx.Equalizer
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mBinding: ActivityAddUpdateDishBinding
    private lateinit var startCameraActivity: ActivityResultLauncher<Intent>
    private lateinit var startGalleryActivity: ActivityResultLauncher<String>
    private var mImagePath: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setupActionBar()
        mBinding.imageViewAddDishImage.setOnClickListener(this)
        // Note this camera activity used the generic activity call contract
        // takes a raw intent as input and activity result as output
        // No type conversion is carried out so we have to do it manually
        startCameraActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                intent?.let { data ->
                    val thumbnail: Bitmap = data.extras!!.get("data") as Bitmap
                    Glide.with(this)
                        .load(thumbnail)
                        .centerCrop()
                        .into(mBinding.imageViewDishImage)

                    saveImageToInternalStorage(thumbnail)

                    mBinding.imageViewAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_vector_edit)
                    )

                }
            }
        }
        /*
        * An ActivityResultContract to prompt the
        * user to pick a piece of content, receiving a content://Uri for that content
        */
        startGalleryActivity = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            Glide.with(this)
                .load(it)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("error", "Error loading image", e)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let {
                            val bitmap: Bitmap = resource.toBitmap()
                            mImagePath = saveImageToInternalStorage(bitmap)

                        }
                        return false
                    }

                })
                .into(mBinding.imageViewDishImage)
            mBinding.imageViewAddDishImage.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_vector_edit)
            )
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(mBinding.toolbarAddDishActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbarAddDishActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.imageView_add_dish_image -> {
                    customImageSelectionDialog()
                }
            }
        }
    }

    private fun customImageSelectionDialog() {
        // create dialog obj
        val dialog: Dialog = Dialog(this)
        // get binding obj generated for the dialog layout we have and inflate
        val binding: DialogCustomImageSelectionBinding =
            DialogCustomImageSelectionBinding.inflate(layoutInflater)
        // set content of the dialog to the layout content, which we obtain from binding.root
        dialog.setContentView(binding.root)
        // on click listeners
        binding.tvCamera.setOnClickListener {
            Dexter.withContext(this@AddUpdateDishActivity)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {
                            if (report.areAllPermissionsGranted()) {
                                startCameraActivityForResult()
                            }
                        }

                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }

                }).onSameThread().check()
            dialog.dismiss()
        }
        binding.tvGallery.setOnClickListener {
            Dexter.withContext(this@AddUpdateDishActivity)
                .withPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                ).withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        startGalleryActivityForResult()
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            "You have denied the storage permission to select image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }

                }).onSameThread().check()
            dialog.dismiss()
        }
        //show dialog
        dialog.show()

    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage(
            "Looks like you turned off permissions " +
                    "required for this feature, kindly turn them on under Application Settings" +
                    "To Proceed"
        ).setPositiveButton("Go to Settings") { _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
    }

    private fun startCameraActivityForResult() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startCameraActivity.launch(intent)
    }

    private fun startGalleryActivityForResult() {
        startGalleryActivity.launch("image/*")
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        // context wrapper is used to identify which application the file to be saved
        // originated from
        val wrapper = ContextWrapper(applicationContext)
        // mode param determines access to file created
        // mode private means only this application, or applications with the same
        // id as this application can access file
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    companion object {
        private const val IMAGE_DIRECTORY = "FavDishImages"
    }
}