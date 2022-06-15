package com.testefoto.appfoto

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import coil.load
import coil.transform.CircleCropTransformation
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.testefoto.appfoto.databinding.ActivityMainBinding
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private val CAMERA_REQUEST_CODE = 1
    private val GALERIA_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate((layoutInflater))
        setContentView(binding.root)

        binding.btnCamera.setOnClickListener {
            cameraCheckPermission()
        }
        binding.btnGaleria.setOnClickListener {
            galeriaCheckPermission()
        }
        //quando você clica na imagem
        binding.imageView.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItem = arrayOf("Selecione a foto da galeria","Use sua camera para tirar uma foto")
            pictureDialog.setItems(pictureDialogItem){dialog, which ->
                when(which){
                    0->galeria()
                    1->cameraCheckPermission()
                }
            }
            pictureDialog.show()
        }
    }
    private fun galeriaCheckPermission(){
        Dexter.withContext(this).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : PermissionListener{
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                galeria()
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                Toast.makeText(this@MainActivity,
                "Permitir app para selecionar imagem",Toast.LENGTH_SHORT).show()
                showRorationalDialogForPermission()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {

            }


        }).onSameThread().check()
    }

    private fun galeria(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/"
        startActivityForResult(intent, GALERIA_REQUEST_CODE)
    }
    //permissoes da camera e poder tirar foto
    private fun cameraCheckPermission(){
        Dexter.withContext(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).withListener(
                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                       report.let{
                           if(report!!.areAllPermissionsGranted()){
                               camera()
                           }
                       }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showRorationalDialogForPermission()
                    }


                }

            ).onSameThread().check()
    }
    private  fun camera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent,CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                CAMERA_REQUEST_CODE->{
                    val bitmap = data?.extras?.get("data") as Bitmap
                    //esse bitmap tem a foto mesma coisa para camera e galeria
                    binding.imageView.load(bitmap){
                       //até aqui ja estava mostrando imagem
                        //deixa a imagem em formato de circulo
                        crossfade(true)
                        crossfade(1000)
                        transformations(CircleCropTransformation())

                    }
                }
                GALERIA_REQUEST_CODE->{
                    binding.imageView.load(data?.data){
                        crossfade(true)
                        crossfade(1000)
                        transformations(CircleCropTransformation())

                    }
                }
            }
        }
    }


    private fun showRorationalDialogForPermission(){
        AlertDialog.Builder(this)
            .setMessage("Parece que você desativou as permissões " + "Pode ser ativado nas configuraçôes do aplicativo")
            .setPositiveButton("Va para configurações"){_,_->
                try {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                    val uri = Uri.fromParts("package", packageName,null)
                    intent.data = uri
                    startActivity(intent)

                }catch (e : ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("CANCEL"){dialog,_->
                dialog.dismiss()
            }.show()
    }
}