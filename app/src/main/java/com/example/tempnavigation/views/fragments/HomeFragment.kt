package com.example.tempnavigation.views.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tempnavigation.R
import com.example.tempnavigation.adapters.NoteViewAdapter
import com.example.tempnavigation.databinding.FragmentHomeBinding
import com.example.tempnavigation.helpers.SwipeHelper
import com.example.tempnavigation.listener.RecyclerItemLongPressListener
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.utilities.DialogUtils
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.AddNoteFragmentViewModel
import com.example.tempnavigation.viewmodels.HomeFragmentViewModel
import com.example.tempnavigation.viewmodels.MainViewModel
import kotlinx.coroutines.launch


class HomeFragment : Fragment(),View.OnClickListener {
    private val TAG = "HomeFrag"
    private val homeFragmentViewModel: HomeFragmentViewModel by viewModels()
    private val addNoteFragmentViewModel:AddNoteFragmentViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private var rootView: View? = null
    private lateinit var adapter: NoteViewAdapter
    private lateinit var bindingHomeFragment: FragmentHomeBinding
    private var isStaggeredView = false
    private lateinit var layoutManager : RecyclerView.LayoutManager
    private var noteList= mutableListOf<NoteModel>()
    private var selectedItems = mutableSetOf<Int>()
    private var selctionIconShow = false
    private var deleteSelectionIconShow = false
    private var deleteAllIconShow = false
    private var archive = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bindingHomeFragment = FragmentHomeBinding.inflate(inflater, container, false)
        rootView = bindingHomeFragment.root
        setHasOptionsMenu(true)
        initView()
        setInitValue()
        observeLiveData()
        return rootView
    }

    private fun initView() {

        val savedState:Boolean? = homeFragmentViewModel.getHomeViewStyle()
        if (savedState != null) {
            isStaggeredView = savedState
        }
        if(savedState == true){
            bindingHomeFragment.recyclerView.layoutManager =  StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
            layoutManager = bindingHomeFragment.recyclerView.layoutManager as StaggeredGridLayoutManager
        }else {
            bindingHomeFragment.recyclerView.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            layoutManager = bindingHomeFragment.recyclerView.layoutManager as LinearLayoutManager
        }
    }
    private fun setInitValue(){
        mainViewModel.showBottomNav.value = true
        mainViewModel.title.value = "Home"
        adapter = NoteViewAdapter(requireContext(),addNoteFragmentViewModel){note->
            mainViewModel.selectedNote.value = note
            addNoteFragmentViewModel.setCurrentNote(note)
            mainViewModel.navigationPage.value = NavigationPage.ADD_NOTE
        }
        bindingHomeFragment.recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : SwipeHelper(bindingHomeFragment.recyclerView){
            override fun instantiateUnderlayButton(position: Int): List<UnderlayButton> {
                val buttonList: List<UnderlayButton>
                val note = noteList[position]
                Log.e(TAG, "instantiateUnderlayButton: ${noteList[position]},/n pos = $position", )
                val deleteButton = UnderlayButton(requireContext(),"Delete",14.0f,R.color.colorChartRed,object:UnderlayButtonClickListener{
                    override fun onClick() {

                        deleteNoteFromList(position)
                        DialogUtils.showSnackBarWithActionButton(
                            requireContext(),
                            requireView(),
                            R.string.delete_undo_msg,
                            R.string.delete_confirm_msg,
                            onUndo = { recoverDeletedNote(position,note) },
                            onTimeout = {
                            homeFragmentViewModel.delete(note,{
                                Log.d(TAG, "trying to delete = $note")
                                DialogUtils.toast(requireContext(),"Deleted Success") },{it->
                                DialogUtils.toast(requireContext(),"$it") })
                        })
                    }

                })
                val archiveButton = UnderlayButton(requireContext(),"Archived",14.0f,R.color.colorGreen,object :UnderlayButtonClickListener{
                    override fun onClick() {
                       archive = true
                       homeFragmentViewModel.update(NoteModel(note.id,note.title,note.description,note.locationLat,note.locationLong,note.imageUri,note.alarmTime,note.savedTime,note.favourite,archive),
                           {
                           Handler(Looper.getMainLooper()).post {
                               DialogUtils.toast(requireContext(),"note has been archived")
                           }
                       },{it->
                           Handler(Looper.getMainLooper()).post {
                               DialogUtils.toast(requireContext(), "$it")
                           }
                       })

                    }

                })
                buttonList = listOf(deleteButton,archiveButton)
                return buttonList
            }

        })
        itemTouchHelper.attachToRecyclerView(bindingHomeFragment.recyclerView)
        val itemLongPressListener = RecyclerItemLongPressListener(requireContext(), bindingHomeFragment.recyclerView) { position ->
            updateTitleBar(true,true,false)
            if (selectedItems.contains(position)) {
                selectedItems.remove(position)
            } else {
                selectedItems.add(position)
            }
            adapter.notifyItemChanged(position)
        }
        bindingHomeFragment.recyclerView.addOnItemTouchListener(itemLongPressListener)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.addButton ->{
                getPermissionForWindowOverlay()
                mainViewModel.selectedNote.value = NoteModel.emptyNote()
                addNoteFragmentViewModel.setCurrentNote(NoteModel.emptyNote())
                Toast.makeText(context,"Create An Add Fragment",Toast.LENGTH_SHORT).show()
                mainViewModel.navigationPage.value = NavigationPage.ADD_NOTE

            }
        }
    }

    private fun observeLiveData(){
        homeFragmentViewModel.allNote.observe(viewLifecycleOwner, Observer { it->
            noteList.clear()

            for (i in it.indices ){

                val note = NoteModel(it[i].id!!,it[i].title,it[i].description,it[i].locationLat,it[i].locationLong,it[i].imgUri,it[i].alarmTime,it[i].savedTime,it[i].favourite,it[i].archive)
                noteList.add(note)
            }

            updateAdapter(noteList)
        })

    }

    private fun updateTitleBar(status:Boolean,selected:Boolean,all:Boolean) {
        selctionIconShow = status
        deleteSelectionIconShow = selected
        deleteAllIconShow = all
        requireActivity().invalidateOptionsMenu()
    }


    private fun updateAdapter(noteList: MutableList<NoteModel>){
        adapter?.setNote(noteList)
    }
    private fun updateData(name:String){
        val list = MutableLiveData<List<NoteModel>>()
        lifecycleScope.launch {
            homeFragmentViewModel.getAllNotes(
                onSuccess = {noteList,successMsg->
                    // list = noteList
                    Toast.makeText(context,successMsg, Toast.LENGTH_SHORT).show()
                    noteList.observe(viewLifecycleOwner, Observer {
                        noteList.value?.let { list ->
                            adapter.setNote(list)
                        }
                    })

                }, onFailed = { errorMsg->
                    Toast.makeText(context, "$TAG $errorMsg", Toast.LENGTH_SHORT).show()

                }
            )
        }

    }
    private fun getPermissionForWindowOverlay():Boolean{
        return if(Settings.canDrawOverlays(context)){

            Toast.makeText(context,"System overlay permission has been granted.",Toast.LENGTH_LONG).show()
            true
        }else{
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context?.packageName}"))
            windowOverlayPermission.launch(intent)
            false
        }
    }
    private val windowOverlayPermission = registerForActivityResult( ActivityResultContracts.StartActivityForResult()){ result->
        if(Settings.canDrawOverlays(context)){
            Toast.makeText(context,"System overlay permission has been granted.",Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(context,"Sorry! this app is unable to display alarm details during active alarm alerts.",Toast.LENGTH_LONG).show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu,menu)
        val toggleItem = menu.findItem(R.id.list_view_icon)
        toggleItem.icon = if(isStaggeredView) AppCompatResources.getDrawable(requireContext(),R.drawable.list)else AppCompatResources.getDrawable(requireContext(),R.drawable.grid)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val paddingInPixels = 8
        val selectAllItem = menu.findItem(R.id.list_view_icon_select_all)
        val unSelectAllItem = menu.findItem(R.id.list_view_icon_unselect_all)
        val deleteAllItem = menu.findItem(R.id.list_view_icon_delete_all)
        val deleteSelectedItem = menu.findItem(R.id.list_view_icon_delete_Selected)
        unSelectAllItem.isVisible = selctionIconShow
        selectAllItem.isVisible = selctionIconShow
        deleteAllItem.isVisible = (selctionIconShow && deleteAllIconShow)
        deleteSelectedItem.isVisible = (selctionIconShow && deleteSelectionIconShow)

        unSelectAllItem.icon?.setBounds(paddingInPixels-4, paddingInPixels,
            unSelectAllItem.icon?.intrinsicWidth?.minus(paddingInPixels) ?: 0,
            unSelectAllItem.icon?.intrinsicHeight?.minus(paddingInPixels) ?: 0
        )
        selectAllItem.icon?.setBounds(paddingInPixels-4, paddingInPixels,
            selectAllItem.icon?.intrinsicWidth?.minus(paddingInPixels) ?: 0,
            selectAllItem.icon?.intrinsicHeight?.minus(paddingInPixels) ?: 0
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.list_view_icon ->{
                var currentView = homeFragmentViewModel.getHomeViewStyle()!!
                currentView = !currentView
                homeFragmentViewModel.updateViewState(currentView)
                isStaggeredView = currentView
                updateViewLayout(currentView)
            requireActivity().invalidateOptionsMenu()
            }
            R.id.list_view_icon_select_all-> {
                selectAllItems()
                updateTitleBar(true,false,true)
            }
            R.id.list_view_icon_unselect_all -> {
                unSelectAllItems()
                updateTitleBar(false,false,false)

            }
            R.id.list_view_icon_delete_all->{
                deleteSelectedItem()
            }
            R.id.list_view_icon_delete_Selected->{
                deleteSelectedItem()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateViewLayout(isStaggeredView:Boolean){
        if (isStaggeredView){
            bindingHomeFragment.recyclerView.layoutManager =  StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        }else{
            bindingHomeFragment.recyclerView.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        }
    }
    fun deleteNoteFromList(pos:Int){
        noteList.removeAt(pos)
        adapter.notifyItemRemoved(pos)
    }
    fun recoverDeletedNote(pos:Int,noteModel: NoteModel){
        noteList.add(pos, noteModel)
        adapter.notifyItemInserted(pos)

    }

    fun selectAllItems(){
        val size = noteList.size
        selectedItems.clear()
        for(i in 0 until  size) {
            selectedItems.add(i)
        }
        //NoteViewAdapter.selectedItems.clear()
        NoteViewAdapter.selectedItems = selectedItems
        adapter.notifyDataSetChanged()
    }
    fun unSelectAllItems(){
        selectedItems.clear()
        NoteViewAdapter.longPressed = false
        adapter.notifyDataSetChanged()
    }
    fun deleteSelectedItem(){
        NoteViewAdapter.longPressed = false
        var flag = false

        selectedItems = NoteViewAdapter.selectedItems
        selectedItems.sortedDescending().forEach{i->
            val note = noteList[i]
            deleteNoteFromList(i)

            homeFragmentViewModel.delete(note,
                onSuccess = {
                    flag = true
                }, onFailed = {
                    flag = false
                })
        }
        if(flag){
            DialogUtils.toast(requireContext(),"${selectedItems.size} note has been deleted successfully!")
        }else{
            DialogUtils.toast(requireContext(),"${selectedItems.size} note deleted is unsuccessful!")
        }


        updateTitleBar(false,false,false)

    }

}