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
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.nubari.favdish.application.FavDishApplication
import com.nubari.favdish.databinding.DialogCustomListBinding
import com.nubari.favdish.model.entities.FavDIsh
import com.nubari.favdish.utils.Constants
import com.nubari.favdish.view.adapters.CustomListItemAdapter
import com.nubari.favdish.viewmodel.FavDishViewModel
import com.nubari.favdish.viewmodel.FavDishViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mBinding: ActivityAddUpdateDishBinding
    private lateinit var startCameraActivity: ActivityResultLauncher<Intent>
    private lateinit var startGalleryActivity: ActivityResultLauncher<String>

    // A global variable for stored image path.
    private var mImagePath: String = ""

    // A global variable for the custom list dialog.
    private lateinit var mCustomListDialog: Dialog

    private var favDishDetails: FavDIsh? = null

    /**
     * To create the ViewModel we used the viewModels delegate, passing in an instance of our FavDishViewModelFactory.
     * This is constructed based on the repository retrieved from the FavDishApplication.
     * Because our view model requires a repository to be created
     */
    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // if the intent that started this activity contains extra
        if (intent.hasExtra(Constants.EXTRA_DISH_DETAILS)) {
            favDishDetails = intent.getParcelableExtra(Constants.EXTRA_DISH_DETAILS)
        }

        //Preload data if the favDish details is not null, ie we are editing
        favDishDetails?.let {
            if (it.id != 0) {
                mImagePath = it.image
                Glide.with(this@AddUpdateDishActivity)
                    .load(mImagePath)
                    .centerCrop()
                    .into(mBinding.imageViewDishImage)

                mBinding.editTextTitle.setText(it.title)
                mBinding.editTextType.setText(it.type)
                mBinding.etCategory.setText(it.category)
                mBinding.etCookingTime.setText(it.cookingTime)
                mBinding.etDirectionToCook.setText(it.directionToCook)

                mBinding.btnAddDish.text = resources.getString(R.string.lbl_update_dish)

            }
        }
        setupActionBar()
        mBinding.imageViewAddDishImage.setOnClickListener(this)
        mBinding.editTextType.setOnClickListener(this)
        mBinding.etCategory.setOnClickListener(this)
        mBinding.etCookingTime.setOnClickListener(this)
        mBinding.btnAddDish.setOnClickListener(this)
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

                    mImagePath = saveImageToInternalStorage(thumbnail)

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
        // if the preloaded dish details is null, this means that we are currently editing
        if (favDishDetails != null && favDishDetails!!.id != 0) {
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_edit_dish)
            }
        } else {
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_add_dish)
            }
        }
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
                    return
                }
                R.id.edit_text_type -> {
                    customItemsDialog(
                        resources.getString(R.string.title_select_dish_type),
                        Constants.dishTypes(),
                        Constants.DISH_TYPE
                    )
                    return
                }
                R.id.et_category -> {
                    customItemsDialog(
                        resources.getString(R.string.title_select_dish_category),
                        Constants.dishCategories(),
                        Constants.DISH_CATEGORY
                    )
                    return
                }
                R.id.et_cooking_time -> {
                    customItemsDialog(
                        resources.getString(R.string.title_select_dish_time),
                        Constants.dishCookingTime(),
                        Constants.DISH_COOKING_TIME
                    )
                    return
                }
                R.id.btn_add_dish -> {
                    val title = mBinding.editTextTitle.text.toString().trim { it <= ' ' }
                    val type = mBinding.editTextType.text.toString().trim { it <= ' ' }
                    val category = mBinding.etCategory.text.toString().trim { it <= ' ' }
                    val ingredients = mBinding.etIngredients.text.toString().trim { it <= ' ' }
                    val cookingTimeInMinutes =
                        mBinding.etCookingTime.text.toString().trim { it <= ' ' }
                    val cookingDirection =
                        mBinding.etDirectionToCook.text.toString().trim { it <= ' ' }
                    when {
                        TextUtils.isEmpty(title) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_title),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(mImagePath) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_image),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(type) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_type),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(category) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_category),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(ingredients) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_ingredients),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(cookingDirection) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_instructions),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(cookingTimeInMinutes) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_time),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            var dishId = 0
                            var imageSource = Constants.DISH_IMAGE_SOURCE_LOCAL
                            var favoriteDish = false
                            favDishDetails?.let {
                                if (it.id != 0) {
                                    dishId = it.id
                                    imageSource = it.imageSource
                                    favoriteDish = it.favoriteDish
                                }
                            }


                            val favDishDetails = FavDIsh(
                                mImagePath,
                                imageSource,
                                title,
                                type,
                                category,
                                ingredients,
                                cookingTimeInMinutes,
                                cookingDirection,
                                favoriteDish,
                                dishId
                            )
                            if (dishId == 0) {
                                mFavDishViewModel.insert(favDishDetails)
                                Toast.makeText(
                                    this@AddUpdateDishActivity,
                                    "Dish Details added successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.i("Insertion", "success")
                            } else {
                                mFavDishViewModel.update(favDishDetails)
                                Toast.makeText(
                                    this@AddUpdateDishActivity,
                                    "Dish Details updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            //finish the activity
                            finish()
                        }
                    }

                }
            }
        }
    }


    fun selectedListItem(item: String, selection: String) {
        when (selection) {
            Constants.DISH_TYPE -> {
                mCustomListDialog.dismiss()
                mBinding.editTextType.setText(item)
            }
            Constants.DISH_CATEGORY -> {
                mCustomListDialog.dismiss()
                mBinding.etCategory.setText(item)
            }
            else -> {
                mCustomListDialog.dismiss()
                mBinding.etCookingTime.setText(item)
            }
        }
    }

    /**
     * A function to launch the custom image selection dialog.
     */
    private fun customImageSelectionDialog() {
        // create dialog obj
        val dialog = Dialog(this)
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

    /**
     * A function used to show the alert dialog when the permissions are denied and need to allow it from settings app info.
     */
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

    /**
     * A function to save a copy of an image to internal storage for FavDishApp to use.
     *
     * @param bitmap
     */
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

    private fun customItemsDialog(title: String, itemsList: List<String>, selection: String) {
        mCustomListDialog = Dialog(this)
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        mCustomListDialog.setContentView(binding.root)
        binding.tvDialogTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(this)
        val adapter = CustomListItemAdapter(this, null, itemsList, selection)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()
    }

    companion object {
        private const val IMAGE_DIRECTORY = "FavDishImages"
    }
}