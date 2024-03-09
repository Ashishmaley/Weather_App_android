package com.example.weatherapp.activity.bottomsheet

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherapp.activity.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.adapter.MyPagerAdapter
import com.example.weatherapp.databinding.FragmentMyBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MyBottomSheetFragment : BottomSheetDialogFragment(){
    private lateinit var fragments: List<Fragment>
    private var isSwipingEnabled = true
    private var binding: FragmentMyBottomSheetBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val existingFragment = childFragmentManager.findFragmentByTag(MyBottomSheetFragment::class.java.simpleName)

        if (existingFragment != null) {
            // Fragment already added, no need to create a new dialog
            return super.onCreateDialog(savedInstanceState)
        }

        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface: DialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(TRANSPARENT)
                BottomSheetBehavior.from(bottomSheet).isHideable = false
                bottomSheet.cancelLongPress()
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager: ViewPager2 = binding?.viewPager ?: return
        val tabLayout: TabLayout = binding?.tabs ?: return
        fragments = listOf(
            HourlyFragment(),
            WeeklyFragment()
        )
        val adapter = MyPagerAdapter(requireActivity(),fragments)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Hourly"
                1 -> tab.text = "Weekly"
            }
        }.attach()

        val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewPager.isUserInputEnabled = false
            }
        }

        viewPager.registerOnPageChangeCallback(onPageChangeCallback)
        viewPager.isUserInputEnabled = true
        dialog?.setOnDismissListener {
            // Bottom sheet is dismissed, make the button visible
            (activity as? MainActivity)?.showButton()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        binding = FragmentMyBottomSheetBinding.inflate(inflater, container, false)
        return binding?.root ?: inflater.inflate(R.layout.fragment_my_bottom_sheet, container, false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}