package ir.duniject.files

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import ir.duniject.files.databinding.DialogAddFileBinding
import ir.duniject.files.databinding.DialogAddFolderBinding
import ir.duniject.files.databinding.DialogDeleteItemBinding
import ir.duniject.files.databinding.FragmentFileBinding
import java.io.File

class FileFragment(val path: String) : Fragment(), FileAdapter.FileEvent {
    lateinit var binding: FragmentFileBinding
    lateinit var adapter: FileAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFileBinding.inflate(layoutInflater)
        return binding.root
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //اولین کاری که باید اینجا انجام بدهیم این است فایلی  که ادرس داشته رو پیدا کنم
        // ما اینجا path  یا همان ادرس را داریم

        val ourFile = File(path)
        binding.txtPath.text = ourFile.name + ">"

        if (ourFile.isDirectory) {

            val listOfFiles = arrayListOf<File>()
            listOfFiles.addAll(ourFile.listFiles()!!)
            listOfFiles.sort()

            adapter = FileAdapter(listOfFiles, this)
            binding.recyclerMain.adapter = adapter
            binding.recyclerMain.layoutManager = GridLayoutManager(context ,
                MainActivity.ourSpanCount,
            LinearLayoutManager.VERTICAL,
            false)

            if (listOfFiles.size > 0) {

                binding.recyclerMain.visibility = View.VISIBLE
                binding.imgNoData.visibility = View.GONE


            } else {
                binding.recyclerMain.visibility = View.GONE
                binding.imgNoData.visibility = View.VISIBLE
            }


        }


        binding.btnAddFolder.setOnClickListener {
            createNewFolder()
        }
        binding.btnAddFile.setOnClickListener {
            createNewFile()
        }

        binding.btnShowType.setOnClickListener {

            if (MainActivity.ourViewType == 0){

                MainActivity.ourViewType = 1
                MainActivity.ourSpanCount = 3

                adapter.changeViewType(MainActivity.ourViewType)
                binding.recyclerMain.layoutManager = GridLayoutManager(context , MainActivity.ourSpanCount)

                binding.btnShowType.setImageResource(R.drawable.ic_grid)

            }else if (MainActivity.ourViewType == 1){

               MainActivity.ourViewType =0
                MainActivity.ourSpanCount = 1

                adapter.changeViewType(MainActivity.ourViewType)
                binding.recyclerMain.layoutManager = GridLayoutManager(context , MainActivity.ourSpanCount)

                binding.btnShowType.setImageResource(R.drawable.ic_list)




            }

        }


    }
    private fun createNewFile() {

            val dialog = AlertDialog.Builder(context).create()

            val addFileBinding = DialogAddFileBinding.inflate(layoutInflater)
            dialog.setView(addFileBinding.root)
            dialog.show()

            addFileBinding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            addFileBinding.btnCreate.setOnClickListener {
                val nameOfNewFolder = addFileBinding.edtAddFolder.text.toString()
                val newFile = File(path + File.separator + nameOfNewFolder)
                //ما نمی توانیم دوتا اسم به صورت مشترک برای دوتا فولدر داشته باشیم و با این خط از کد امدیم و این موضوع را چک کردیم
                // در واقعه گفتیم اگه این فتیلی که ما ساختیم با این اسم نیست
                if (!newFile.exists()) {
                    //حالا شما بیا و با این دستور پایینی فولدر ما را بساز
                    if (newFile.createNewFile()) {
                        // حالا فولدری که ساختیم را باید از طریق اداتر به ریسایکلر ویو خودمان اضافه کنیم
                        adapter.newFolder(newFile, 0)
                        //زمانی که یک ایتم جدید هم ساخته می شود ما باید ان را در ریسایکلر ویو خودمان ست کنیم
                        binding.recyclerMain.scrollToPosition(0)
                        binding.recyclerMain.visibility = View.VISIBLE
                        binding.imgNoData.visibility=View.GONE

                    }
                }
                dialog.dismiss()
            }


    }
    private fun createNewFolder() {
        val dialog = AlertDialog.Builder(context).create()

        val addFolderBinding = DialogAddFolderBinding.inflate(layoutInflater)
        dialog.setView(addFolderBinding.root)
        dialog.show()

        addFolderBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        addFolderBinding.btnCreate.setOnClickListener {
            val nameOfNewFolder = addFolderBinding.edtAddFolder.text.toString()
            val newFile = File(path + File.separator + nameOfNewFolder)
            //ما نمی توانیم دوتا اسم به صورت مشترک برای دوتا فولدر داشته باشیم و با این خط از کد امدیم و این موضوع را چک کردیم
            // در واقعه گفتیم اگه این فتیلی که ما ساختیم با این اسم نیست
            if (!newFile.exists()) {
                //حالا شما بیا و با این دستور پایینی فولدر ما را بساز
                if (newFile.mkdir()) {
                    // حالا فولدری که ساختیم را باید از طریق اداتر به ریسایکلر ویو خودمان اضافه کنیم
                    adapter.newFolder(newFile, 0)
                    //زمانی که یک ایتم جدید هم ساخته می شود ما باید ان را در ریسایکلر ویو خودمان ست کنیم
                    binding.recyclerMain.scrollToPosition(0)
                    binding.recyclerMain.visibility = View.VISIBLE
                    binding.imgNoData.visibility=View.GONE

                }
            }
            dialog.dismiss()
        }
    }

    override fun onFileClicked(file: File, type: String) {

        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            val fileProvider = FileProvider.getUriForFile(
                requireContext(),
                requireActivity().packageName + ".provider",
                file
            )
            intent.setDataAndType(fileProvider, type)
        } else {
            intent.setDataAndType(Uri.fromFile(file), type)
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }
    override fun onFolderClicked(path: String) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_main_container, FileFragment(path))
        transaction.addToBackStack(null)
        transaction.commit()
    }
    override fun onLongClicked(file: File, position: Int) {

        val dialog = AlertDialog.Builder(context).create()
        val removeDialogBinding = DialogDeleteItemBinding.inflate(layoutInflater)
        dialog.setView(removeDialogBinding.root)
        dialog.show()

        removeDialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        removeDialogBinding.btnDelete.setOnClickListener {

            if (file.exists()) {
                if (file.deleteRecursively()) {
                    adapter.removeFile(file, position)
                }
            }

            dialog.dismiss()
        }


    }
}