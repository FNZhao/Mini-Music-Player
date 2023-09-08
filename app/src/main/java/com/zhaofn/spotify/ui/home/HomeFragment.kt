package com.zhaofn.spotify.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {//Fragment可以提供navController
    private val viewModel: HomeViewModel by viewModels() //这是一个固定写法，这样写的话系统会自动inject,实际的instance还是被inject的
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //findNavController()//fragment可以去把自己换成别的fragment

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_home, container, false)//这是老的方法
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(colors = darkColors()) {
                    HomeScreen(viewModel, onTap = {
                        Log.d("HomeFragment", "we tapped ${it.name}")//it指代的是Album，指的是callback传回来的arg//可以改成${album.name}
                        val direction = HomeFragmentDirections.actionHomeFragmentToPlaylistFragment(it)
                        findNavController().navigate(directions = direction)
                    })
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {//viewmodel不会在旋转屏幕时摧毁重建，所以要在这里保存state//离开页面照样会消失
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.uiState.value.isLoading) {//当进入这个页面，fragment显示的时候，view通知viewmodel获取数据
            viewModel.fetchHomeScreen()
        }
    }
}