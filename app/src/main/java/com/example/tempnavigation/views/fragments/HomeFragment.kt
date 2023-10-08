package com.example.tempnavigation.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tempnavigation.R
import com.example.tempnavigation.adapters.NoteViewAdapter
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.HomeFragmentViewModel
import com.example.tempnavigation.viewmodels.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class HomeFragment : Fragment(),View.OnClickListener {
    private val TAG = "HomeFrag"
    private val homeFragmentViewModel: HomeFragmentViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var addButton: FloatingActionButton
    private lateinit var listView: RecyclerView
    private  var rootView: View? = null
    private lateinit var toolbar: Toolbar
    private  lateinit var adapter: NoteViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_home, container, false)
        //initToolbar()
        initView()
        setInitValue()
        observeLiveData()
        //updateData("onCreate")
        return rootView
    }

    override fun onResume() {
        super.onResume()
    }
    private fun initView(){
        // toolbar = requireView().findViewById(R.id.toolbar)
        listView = rootView?.findViewById(R.id.recyclerView)!!
        listView.layoutManager = LinearLayoutManager(requireContext())
//        addButton = rootView.findViewById(R.id.addButton)
        //addButton.setOnClickListener(this)
    }
    private fun setInitValue(){
        mainViewModel.showBottomNav.value = true
        mainViewModel.title.value = "Home"
        adapter = NoteViewAdapter(){note->
            mainViewModel.selectedNote.value = note
            mainViewModel.navigationPage.value = NavigationPage.EDIT_NOTE
        }
        listView.adapter = adapter
        //dummyData()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.addButton ->{
                //Toast.makeText(context,"Create An Add Fragment",Toast.LENGTH_SHORT).show()
                mainViewModel.navigationPage.value = NavigationPage.ADD_NOTE
            }
        }
    }
//    private fun updateText(num:Int):Int{
//        if (num==0) i=0
//        i +=num
//        return i
//    }

    private fun observeLiveData(){
        homeFragmentViewModel.allNote.observe(viewLifecycleOwner, Observer { it->
            val list = mutableListOf<NoteModel>()
            for (i in it.indices ){
                val note = NoteModel(it[i].id,it[i].title,it[i].description,it[i].priority,it[i].imgUri)
                list.add(note)
            }
            adapter.setNote(list)
        })
    }
    private fun updateData(name:String){
        var list = MutableLiveData<List<NoteModel>>()
        lifecycleScope.launch {
            homeFragmentViewModel.getAllNotes(
                onSuccess = {noteList,successMsg->
                    // list = noteList
                    Toast.makeText(context,successMsg, Toast.LENGTH_SHORT).show()
                    noteList.observe(viewLifecycleOwner, Observer {
                        noteList.value?.let { list ->
                            adapter.setNote(list).also {
                                Log.d(TAG, "adapter called from $name")
                            }
                        }
                    })

                }, onFailed = { errorMsg->
                    Toast.makeText(context, "$TAG $errorMsg", Toast.LENGTH_SHORT).show()

                }
            )
        }
        Log.d(TAG, "i've been called from $name & list size = ${list.value?.size}")

        //adapter.setNote(list)

    }
    fun dummyData(){
        val list: MutableList<NoteModel> = mutableListOf()
        list.add( NoteModel(1,"hasan","name",1,""))
        list.add( NoteModel(2,"Jishan","name",2,""))
        list.add( NoteModel(3,"Noman","name",3,""))
        list.add( NoteModel(4,"Puspa","name",4,""))
        list.add( NoteModel(5,"Farhan","name",5,""))

        //adapter.setNote(list)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }
}